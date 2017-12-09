package me.wieku.circuits.world.elements.gates

import me.wieku.circuits.api.element.BasicGate
import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.state.State
import me.wieku.circuits.api.world.IWorld

class TFFGate(pos: Vector2i): BasicGate(pos) {

	private lateinit var state: State
	private var toUpdate = true

	override fun update(tick: Long) {
		var calc = false
		for(i in 0 until inputs.size)
			calc = calc || inputs[i].isActive()

		if(calc) {
			if(toUpdate) {
				state.setActive(!state.isActive())
				toUpdate = false
			}
		} else {
			toUpdate = true
		}

		setOut(state.isActive())
	}

	override fun onPlace(world: IWorld) {
		super.onPlace(world)
		state = world.getStateManager()()
	}

	override fun onRemove(world: IWorld) {
		setOut(false)
		state.unregister()
	}

	override fun getIdleColor(): Int = 0x311B92

	override fun getActiveColor(): Int = 0x4527A0

	override fun getColor(): Int = if (state.isActiveD()) getActiveColor() else getIdleColor()

	override fun setState(state: State, axis: Axis) {}

	override fun getState(axis: Axis): State = state
}