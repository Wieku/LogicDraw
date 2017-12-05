package me.wieku.circuits.world

import me.wieku.circuits.api.element.IElement
import me.wieku.circuits.api.element.ITickable
import me.wieku.circuits.api.math.Direction
import me.wieku.circuits.api.math.Rectangle
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.state.StateManager
import me.wieku.circuits.api.world.IWorld
import me.wieku.circuits.world.elements.Cross
import me.wieku.circuits.world.elements.Input
import me.wieku.circuits.world.elements.Wire
import me.wieku.circuits.world.elements.gates.*
import java.util.*

//TODO: switch to tasks instead of locking objects
class ClassicWorld(val width: Int, val height: Int):IWorld {
	private val manager: StateManager = StateManager(width*height)
	private val map: Array<Array<IElement?>> = Array(width) { Array<IElement?>(height) {null} }
	private val tickables: HashMap<Vector2i, ITickable> = HashMap()
	val classes: HashMap<String, Class<out IElement>> = HashMap()

	init {
		classes.put("Wire", Wire::class.java)
		classes.put("Cross", Cross::class.java)
		classes.put("Input", Input::class.java)
		classes.put("Or", OrGate::class.java)
		classes.put("Nor", NorGate::class.java)
		classes.put("And", AndGate::class.java)
		classes.put("Nand", NandGate::class.java)
		classes.put("Xor", XorGate::class.java)
		classes.put("Xnor", XnorGate::class.java)
	}


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
		if(classes.containsKey(name)) {
			placeElement(position, classes[name]!!)
		} else {
			println("[ERROR} Element doesn't exist!")
		}
	}

	private fun placeElement(position: Vector2i, clazz: Class<out IElement>) {
		if(!position.isInBounds(0, 0, width-1, height-1)) return
		if(map[position.x][position.y] != null) removeElement(position)
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

	private var tempVec = Vector2i()
	fun clear(rectangle: Rectangle) {
		for (x in rectangle.x until rectangle.x + rectangle.width) {
			for (y in rectangle.y until rectangle.y + rectangle.height) {
				removeElement(tempVec.set(x, y))
			}
		}
	}

	fun paste(position: Vector2i, clipboard: WorldClipboard) {
		for(x in 0 until clipboard.selection.width) {
			for (y in 0 until clipboard.selection.height) {
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

	override fun updateNeighboursOf(pos: Vector2i) {
		getNeighboursOf(pos).forEach { it.onNeighbourChange(pos, this) }
	}

	override fun getStateManager(): StateManager = manager

	operator fun get(x:Int, y:Int) = map[x][y]
}