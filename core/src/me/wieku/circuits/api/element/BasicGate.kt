package me.wieku.circuits.api.element

import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.world.IWorld
import java.util.*

abstract class BasicGate(pos: Vector2i): BasicElement(pos),ITickable {

	protected val inputs = ArrayList<BasicInput>()

	private val outputs = ArrayList<BasicWire>()
	private val axes = ArrayList<Axis>()
	protected var dirty = true

	override fun onPlace(world: IWorld) {
		updateIO(world)
	}

	override fun onNeighbourChange(position: Vector2i, world: IWorld) {
		updateIO(world)
		dirty = true
	}

	protected fun setOut(value: Boolean) {
		for(i in 0 until outputs.size) outputs[i].getState(axes[i]).setActive(value)
	}

	private fun updateIO(world: IWorld) {
		inputs.clear()
		outputs.clear()
		axes.clear()

		world.getNeighboursOf(this) {
			when(it) {
				is BasicInput -> inputs += it
				is BasicWire -> {
					outputs += it
					axes += Axis.getAxis(getPosition(), it.getPosition())
				}
			}
		}
	}

}