package me.wieku.circuits.world

import me.wieku.circuits.api.element.IElement
import me.wieku.circuits.api.element.ITickable
import me.wieku.circuits.api.math.Direction
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.state.StateManager
import me.wieku.circuits.api.world.IWorld
import java.util.*

class ClassicWorld(private val width: Int, private val height: Int):IWorld {

	private val manager: StateManager = StateManager(width*height)
	private val map: Array<Array<IElement?>> = Array(width) { Array<IElement?>(height) {null} }
	private val tickables: HashMap<Vector2i, ITickable> = HashMap()

	override fun update(tick: Long) {
		tickables.entries.forEach { it.value.update(tick) }
		manager.swap()
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
}