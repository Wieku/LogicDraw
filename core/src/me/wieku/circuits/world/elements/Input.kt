package me.wieku.circuits.world.elements

import me.wieku.circuits.api.element.BasicInput
import me.wieku.circuits.api.element.BasicWire
import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.state.State
import me.wieku.circuits.api.world.IWorld
import java.util.*

class Input(pos: Vector2i):BasicInput(pos) {

	private lateinit var intSt: State
	private val inputs = ArrayList<State>()

	override fun isActive(): Boolean {
		intSt.setActive(false)
		if(inputs.isNotEmpty()) {
			for(i in 0 until inputs.size) {
				if(inputs[i].isActive()) {
					intSt.setActive(true)
					return true
				}
			}
		}
		return false
	}

	override fun onPlace(world: IWorld) {
		intSt = world.getStateManager()()
		updateI(world)
		world.updateNeighboursOf(pos)
	}

	override fun onNeighbourChange(position: Vector2i, world: IWorld) {
		updateI(world)
		//world.updateNeighboursOf(pos)
	}

	private fun updateI(world: IWorld) {
		inputs.clear()

		world.getNeighboursOf(this) {
			when(it) {
				is BasicWire -> {
					inputs += it.getState(Axis.getAxis(getPosition(), it.getPosition()))
				}
			}
		}
	}

	override fun getIdleColor(): Int = 0x01579B

	override fun getActiveColor(): Int = 0x0277BD

	override fun getColor(): Int = if(isActive()) getActiveColor() else getIdleColor()

	override fun setState(state: State, axis: Axis) {}

	override fun getState(axis: Axis): State = intSt
}