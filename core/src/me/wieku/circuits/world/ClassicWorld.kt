package me.wieku.circuits.world

import me.wieku.circuits.api.element.IElement
import me.wieku.circuits.api.element.ITickable
import me.wieku.circuits.api.math.Direction
import me.wieku.circuits.api.math.Rectangle
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.state.StateManager
import me.wieku.circuits.api.world.IWorld
import me.wieku.circuits.api.world.clock.AsyncClock
import java.util.*

//TODO: switch to tasks instead of locking objects
class ClassicWorld(val width: Int, val height: Int, val name: String):IWorld {
	private val manager: ClassicStateManager = ClassicStateManager(width * height)
	private val map: Array<Array<IElement?>> = Array(width) { Array<IElement?>(height) {null} }
	private val tickables: HashMap<Vector2i, ITickable> = HashMap()

	var clock: AsyncClock? = null

	var entities = 0
	get() = tickables.size
	private set

	override fun update(tick: Long) {
		try {
			synchronized(tickables) {
				tickables.entries.forEach { it.value.update(tick) }
				synchronized(manager) {
					manager.swap()
				}
			}
		} catch (i: Exception) {
			i.printStackTrace()
		}
	}

	override fun placeElement(position: Vector2i, name: String) {
		if(ElementRegistry.classes.containsKey(name)) {
			placeElement(position, ElementRegistry.classes[name]!!)
		} else {
			println("[ERROR] Element doesn't exist!")
		}
	}

	private fun placeElement(position: Vector2i, clazz: Class<out IElement>) {
		if(!position.isInBounds(0, 0, width-1, height-1)) return
		if(map[position.x][position.y] != null) {
			if(map[position.x][position.y]!!.javaClass != clazz) {
				removeElement(position)
			} else return
		}
		var el:IElement = clazz.getConstructor(Vector2i::class.java).newInstance(position)
		synchronized(map) {
			map[position.x][position.y] = el
			el.onPlace(this)
			if(el is ITickable) {
				synchronized(tickables) {
					tickables.put(position, el)
				}
			}
		}
	}

	fun forcePlace(x: Int, y: Int, name: String): IElement {
		if(ElementRegistry.classes.containsKey(name)) {
			var position = Vector2i(x, y)
			var el:IElement = ElementRegistry.classes[name]!!.getConstructor(Vector2i::class.java).newInstance(position)
			map[position.x][position.y] = el
			if(el is ITickable) {
				tickables.put(position, el)
			}
			return el
		} else {
			throw IllegalStateException("Element $name doesn't exist!")
		}
	}

	private var tempVec = Vector2i()
	fun clear(rectangle: Rectangle) {
		for (x in rectangle.x until rectangle.x + rectangle.width) {
			for (y in rectangle.y until rectangle.y + rectangle.height) {
				removeElement(tempVec.set(x, y))
			}
		}
	}

	fun fill(rectangle: Rectangle, toPlace: String) {
		for (x in rectangle.x until rectangle.x + rectangle.width) {
			for (y in rectangle.y until rectangle.y + rectangle.height) {
				placeElement(Vector2i(x, y), toPlace)
			}
		}
	}

	fun paste(position: Vector2i, clipboard: WorldClipboard) {
		for(x in 0 until clipboard.width) {
			for (y in 0 until clipboard.height) {
				if(clipboard[x, y] != null)
					placeElement(Vector2i(x, y).add(position), clipboard[x, y]!!.javaClass)
				else
					removeElement(Vector2i(x, y).add(position))
			}
		}
	}

	override fun removeElement(position: Vector2i) {
		if(!position.isInBounds(0, 0, width-1, height-1)) return
		var element: IElement? = map[position.x][position.y] ?: return

		synchronized(map) {
			synchronized(tickables) {
				tickables.remove(position)
			}
			map[position.x][position.y] = null
			element!!.onRemove(this)
		}
	}

	override fun getElement(position: Vector2i) = if(position.isInBounds(0, 0, width - 1, height - 1)) map[position.x][position.y] else null

	private fun getNeighboursOf(position: Vector2i): Array<IElement> {
		var list = ArrayList<IElement>()
		Direction.VALID_DIRECTIONS.forEach {
			var tmpE = getElement(position + it.asVector)
			if (tmpE != null) list.add(tmpE)
		}
		return list.toTypedArray()
	}

	override fun getNeighboursOf(element: IElement): Array<IElement> = getNeighboursOf(element.getPosition())

	override fun getNeighboursOf(element: IElement, consumer: (IElement) -> Unit) = getNeighboursOf(element).forEach(consumer)

	private val stack = ArrayDeque<Vector2i>()
	private var first = true
	private var tempVector = Vector2i()
	override fun updateNeighboursOf(pos: Vector2i) {
		stack.push(pos)
		if(first) {
			first = false
			while(stack.isNotEmpty()) {
				val position = stack.pop()
				for(i in 0 until Direction.VALID_DIRECTIONS.size) {
					tempVector.set(position).add(Direction.VALID_DIRECTIONS[i].asVector)
					val tmpE = getElement(tempVector)
					if (tmpE != null) tmpE.onNeighbourChange(position, this)
				}
			}
			first = true
		}
	}

	override fun getStateManager(): StateManager = manager

	operator fun get(x:Int, y:Int) = map[x][y]
}