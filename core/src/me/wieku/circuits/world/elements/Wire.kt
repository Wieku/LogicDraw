package me.wieku.circuits.world.elements

import me.wieku.circuits.api.element.BasicWire
import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.state.State
import me.wieku.circuits.api.world.IWorld

class Wire(pos: Vector2i): BasicWire(pos) {

	lateinit var state:State

	override fun onPlace(world: IWorld) {
		var list = world.getNeighboursOf(this)
		if(list.isNotEmpty()) {
			var ix = 0
			var size = 0
			for(i in 0 until list.size) {
				var hol = list[i].getState(Axis.getAxis(pos, list[i].getPosition())).holders
				if(hol > size) {
					ix = i
					size = hol
				}
			}
			state = list[ix].getState(Axis.getAxis(pos, list[ix].getPosition()))
			world.updateNeighboursOf(pos)
		} else {
			state = world.getStateManager()()
		}
		state.register()
	}

	override fun onNeighbourChange(position: Vector2i, world: IWorld) {
		if (world.getElement(position) == null) {
			state.unregister()
			state = world.getStateManager()()
			state.register()
			world.updateNeighboursOf(pos)
		} else {
			var stateU = world.getElement(position)!!.getState(Axis.getAxis(pos, position))
			if(state != stateU) {
				state.unregister()
				state = stateU
				state.register()
			}
		}
	}

	override fun getIdleColor(): Int = 0x7F0000

	override fun getActiveColor(): Int = 0xD50000

	override fun getColor(): Int = if(state.isActiveD()) getActiveColor() else getIdleColor()

	override fun setState(state: State, axis: Axis) {}

	override fun getState(axis: Axis): State = state
}