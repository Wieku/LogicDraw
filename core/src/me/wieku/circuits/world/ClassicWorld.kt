package me.wieku.circuits.world

import com.google.common.eventbus.EventBus
import me.wieku.circuits.api.collections.Array2D
import me.wieku.circuits.api.element.IElement
import me.wieku.circuits.api.element.gates.ITickable
import me.wieku.circuits.api.element.edit.Copyable
import me.wieku.circuits.api.math.Direction
import me.wieku.circuits.api.collections.MappedArray
import me.wieku.circuits.api.element.gates.ITickableAlways
import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Rectangle
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.state.StateManager
import me.wieku.circuits.api.world.IWorld
import me.wieku.circuits.api.world.clock.AsyncClock
import me.wieku.circuits.render.map.WorldRenderer
import me.wieku.circuits.world.state.ClassicStateManager
import java.util.*

class ClassicWorld(val width: Int, val height: Int, val name: String):IWorld {
	private val manager: ClassicStateManager = ClassicStateManager(width * height)
	private val map: Array2D<IElement?> = Array2D(width, height)

	private val tickables = MappedArray<Vector2i, ITickable>(width * height)
	private val constantTickables = MappedArray<Vector2i, ITickable>(width * height)

	private val tickableQueueA = ArrayDeque<ITickable>(10000)
	private val tickableQueueB = ArrayDeque<ITickable>(10000)
	private var useB = false


	private val tasks = ArrayDeque<Runnable>()
	val eventBus = EventBus("buttons")

	var clock: AsyncClock? = null

	var entities = 0
	get() = tickables.currentElements
	private set

	var worldRenderer: WorldRenderer? = null
		set(value) {
			if (value != null) {
				for (x in 0 until width) {
					for (y in 0 until height) {
						val el = map[x][y]
						if (el != null) {
							value.setElementData(x, y, el.getIdleColor(), el.getActiveColor())
						} else {
							value.setElementData(x, y, 0, 0)
						}

						elementStateUpdated(Vector2i(x, y))
					}
				}
			}

			manager.worldRenderer = value
			field = value
		}

	override fun update(tick: Long) {
		val queue = if (useB) tickableQueueA else tickableQueueB
		for (t in queue) {
			t.isAlreadyMarked = false
		}

		for(i in 0 until constantTickables.size) constantTickables[i]?.update(tick)

		while (queue.isNotEmpty()) queue.poll().update(tick)

		useB = !useB

		updateTasks()

		manager.swap()
	}

	fun updateTasks() {
		while(tasks.isNotEmpty()) tasks.poll().run()
	}

	override fun placeElement(position: Vector2i, name: String) {
		if(ElementRegistry.contains(name)) {
			placeElement(position, ElementRegistry.get(name)!!)
		} else {
			println("[ERROR] Element doesn't exist!")
		}
	}

	private fun placeElementNT(position: Vector2i, name: String) {
		if(ElementRegistry.contains(name)) {
			placeElementNT(position, ElementRegistry.get(name)!!)
		} else {
			println("[ERROR] Element doesn't exist!")
		}
	}

	private fun placeElement(position: Vector2i, clazz: Class<out IElement>) {
		tasks.add(Runnable {
			placeElementNT(position, clazz)
		})
	}

	private fun placeElementNT(position: Vector2i, clazz: Class<out IElement>) {
		if(!position.isInBounds(0, 0, width-1, height-1)) return
		if(map[position.x][position.y] != null) {
			if(map[position.x][position.y]!!.javaClass != clazz) {
				removeElementNT(position)
			} else return
		}
		val el:IElement = ElementRegistry.create(clazz, position)
		map[position.x][position.y] = el
		el.onPlace(this)
		if(el is ITickable) {
			tickables.put(position, el)
			if (el is ITickableAlways) {
				constantTickables.put(position, el)
			} else {
				markForUpdate(el)
			}
		}
		worldRenderer?.setElementData(position.x, position.y, el.getIdleColor(), el.getActiveColor())
	}

	fun forcePlace(x: Int, y: Int, name: String): IElement {
		if(ElementRegistry.contains(name)) {
			val position = Vector2i(x, y)
			val el:IElement = ElementRegistry.create(name, position)
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
		tasks.add(Runnable {
			clearNT(rectangle)
		})
	}

	private fun clearNT(rectangle: Rectangle) {
		if(rectangle.width > 2 && rectangle.height > 2) {
			lock = true
			for (x in rectangle.x+1 until rectangle.x + rectangle.width-1) {
				for (y in rectangle.y+1 until rectangle.y + rectangle.height-1) {
					removeElementNT(tempVec.set(x, y))
				}
			}
			lock = false

			for (x in rectangle.x until rectangle.x + rectangle.width) {
				removeElementNT(tempVec.set(x, rectangle.y))
				removeElementNT(tempVec.set(x, rectangle.y + rectangle.height-1))
			}

			for (y in rectangle.y until rectangle.y + rectangle.height) {
				removeElementNT(tempVec.set(rectangle.x, y))
				removeElementNT(tempVec.set(rectangle.x + rectangle.width - 1, y))
			}

		} else {
			for (x in rectangle.x until rectangle.x + rectangle.width) {
				for (y in rectangle.y until rectangle.y + rectangle.height) {
					removeElementNT(tempVec.set(x, y))
				}
			}
		}
	}

	fun fill(rectangle: Rectangle, toPlace: String) {
		tasks.add(Runnable {
			clearNT(rectangle)
			for (x in rectangle.x until rectangle.x + rectangle.width) {
				for (y in rectangle.y until rectangle.y + rectangle.height) {
					placeElementNT(Vector2i(x, y), toPlace)
				}
			}
		})
	}

	fun paste(position: Vector2i, clipboard: WorldClipboard) {
		tasks.add(Runnable {
			pasteNT(position, clipboard)
		})
	}

	fun stack(rectangle: Rectangle, direction: Direction, amount: Int) {
		tasks.add(Runnable {
			stackNT(rectangle, direction, amount)
		})
	}

	fun stackNT(rectangle: Rectangle, direction: Direction, amount: Int) {
		val clipboard = WorldClipboard.create(rectangle, this)
		val vec = direction.asVector
		val tmpVec = Vector2i()
		for(d in 1..amount) {
			tmpVec.set(vec).scl(rectangle.width, rectangle.height).scl(d.toFloat()).add(rectangle.x,rectangle.y)
			if(!tmpVec.isInBounds(0, 0, width, height)) break
			pasteNT(tmpVec, clipboard)
		}
	}

	fun pasteNT(position: Vector2i, clipboard: WorldClipboard) {
		clearNT(Rectangle(position, Vector2i(position.x+clipboard.width, position.y + clipboard.height)))
		for(x in 0 until clipboard.width) {
			for (y in 0 until clipboard.height) {
				if(clipboard[x, y] != null) {
					val pos = Vector2i(x, y).add(position)
					placeElementNT(pos, clipboard[x, y]!!.javaClass)
				} else
					removeElementNT(Vector2i(x, y).add(position))
			}
		}

		if(clipboard.data != null) {
			for(x in 0 until clipboard.width) {
				for (y in 0 until clipboard.height) {
					val pos = Vector2i(x, y).add(position)
					val element = getElement(pos)
					if(element is Copyable) {
						if(clipboard.data[x][y] != null) {
							element.pasteData(clipboard.data[x][y]!!)
						}
					}
				}
			}
			manager.swap()
		}
	}

	override fun removeElement(position: Vector2i) {
		tasks.add(Runnable {
			removeElementNT(position)
		})
	}

	private fun removeElementNT(position: Vector2i) {
		if(!position.isInBounds(0, 0, width-1, height-1)) return
		val element: IElement? = map[position.x][position.y] ?: return
		tickables.remove(position)
		constantTickables.remove(position)
		map[position.x][position.y] = null
		element!!.onRemove(this)
		worldRenderer?.setElementData(position.x, position.y, 0, 0)
	}

	override fun getElement(position: Vector2i) = if(position.isInBounds(0, 0, width - 1, height - 1)) map[position.x][position.y] else null

	private fun getNeighboursOf(position: Vector2i): Array<IElement> {
		val list = ArrayList<IElement>()
		Direction.VALID_DIRECTIONS.forEach {
			val tmpE = getElement(position + it.asVector)
			if (tmpE != null) list.add(tmpE)
		}
		return list.toTypedArray()
	}

	override fun getNeighboursOf(element: IElement): Array<IElement> = getNeighboursOf(element.getPosition())

	override fun getNeighboursOf(element: IElement, consumer: (IElement) -> Unit) = getNeighboursOf(element).forEach(consumer)

	private val stack = ArrayDeque<Vector2i>()
	private var first = true
	private var tempVector = Vector2i()
	private var lock = false
	override fun updateNeighboursOf(pos: Vector2i) {
		if(lock) return
		stack.push(pos)
		if(first) {
			first = false
			while(stack.isNotEmpty()) {
				val position = stack.pop()
				for(element in Direction.VALID_DIRECTIONS) {
					tempVector.set(position).add(element.asVector)
					getElement(tempVector)?.onNeighbourChange(position, this)
				}
			}
			first = true
		}
	}

	override fun elementStateUpdated(pos: Vector2i) {
		val it = getElement(pos)
		if (it != null) {
			val horizontal = it.getState(Axis.HORIZONTAL)
			val vertical = it.getState(Axis.VERTICAL)
			worldRenderer?.setElementStateData(pos.x, pos.y, horizontal?.id ?: 0, vertical?.id ?: 0)
		}
	}

	override fun markForUpdate(tickable: ITickable) {
		if (!tickable.isAlreadyMarked && tickable !is ITickableAlways) {
			val queue = if (useB) tickableQueueB else tickableQueueA
			tickable.isAlreadyMarked = true
			queue.push(tickable)
		}
	}

	override fun getStateManager(): StateManager = manager

	operator fun get(x:Int, y:Int) = map[x][y]
}