package me.wieku.circuits.api.element

import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.state.State
import me.wieku.circuits.api.world.IWorld
import java.util.*

abstract class BasicGate(pos: Vector2i): BasicElement(pos),ITickable {

	protected val inputs = ArrayList<BasicInput>()
	protected val outputs = ArrayList<State>()

	override fun onPlace(world: IWorld) {
		updateIO(world)
	}

	override fun onNeighbourChange(position: Vector2i, world: IWorld) {
		updateIO(world)
	}

	protected fun setOut(value: Boolean) {
		for(i in 0 until outputs.size) outputs[i].setActive(value)
	}

	protected open fun updateIO(world: IWorld) {
		inputs.clear()
		outputs.clear()

		world.getNeighboursOf(this) {
			when(it) {
				is BasicInput -> inputs += it
				is BasicWire -> {
					outputs += it.getState(Axis.getAxis(getPosition(), it.getPosition()))
				}
			}
		}
	}

}