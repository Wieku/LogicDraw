package me.wieku.circuits.api.element

import me.wieku.circuits.api.element.holders.Outputs
import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.world.IWorld

abstract class BasicOutput(pos: Vector2i): BasicElement(pos) {

	protected val outputs = Outputs()

	override fun onPlace(world: IWorld) {
		updateIO(world)
	}

	override fun onNeighbourChange(position: Vector2i, world: IWorld) {
		updateIO(world)
	}

	open fun setOut(value: Boolean) {
		outputs.setActive(value)
	}

	protected open fun updateIO(world: IWorld) {
		outputs.clear()

		world.getNeighboursOf(this) {
			when(it) {
				is BasicWire -> {
					val state = it.getState(Axis.getAxis(getPosition(), it.getPosition()))
					if(state != null)
						outputs += state
				}
			}
		}
	}

}