package me.wieku.circuits.world

import me.wieku.circuits.api.element.BasicElement
import me.wieku.circuits.api.element.IElement
import me.wieku.circuits.api.element.ITickable
import me.wieku.circuits.api.math.Direction
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.state.StateManager
import me.wieku.circuits.api.world.IWorld
import me.wieku.circuits.world.elements.Cross
import me.wieku.circuits.world.elements.Input
import me.wieku.circuits.world.elements.Wire
import me.wieku.circuits.world.elements.gates.*
import java.util.*

class ClassicWorld(val width: Int, val height: Int):IWorld {
	private val manager: StateManager = StateManager(width*height)
	private val map: Array<Array<IElement?>> = Array(width) { Array<IElement?>(height) {null} }
	private val tickables: HashMap<Vector2i, ITickable> = HashMap()
	val classes: HashMap<String, Class<out BasicElement>> = HashMap()

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
		tickables.entries.forEach { it.value.update(tick) }
		manager.swap()
	}

	override fun placeElement(position: Vector2i, name: String) {
		if(!position.isInBounds(0, 0, width-1, height-1)) return
		if(map[position.x][position.y] != null) removeElement(position)
		if(classes.containsKey(name)) {
			var el:BasicElement = classes[name]!!.getConstructor(Vector2i::class.java).newInstance(position)
			map[position.x][position.y] = el
			el.onPlace(this)
			if(el is ITickable) tickables.put(position, el)
		} else {
			println("[ERROR} Element doesn't exist!")
		}
	}

	override fun removeElement(position: Vector2i) {
		if(!position.isInBounds(0, 0, width-1, height-1)) return
		var element: IElement? = map[position.x][position.y] ?: return

		tickables.remove(position)
		map[position.x][position.y] = null
		element!!.onRemove(this)
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