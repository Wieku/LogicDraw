package me.wieku.circuits.world.elements

import me.wieku.circuits.api.element.BasicWire
import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.state.State
import me.wieku.circuits.api.world.IWorld

class Cross(pos: Vector2i): BasicWire(pos) {

	lateinit var stateH:State
	lateinit var stateV:State

	override fun onPlace(world: IWorld) {
		TODO("not implemented yet")
	}

	override fun onNeighbourChange(position: Vector2i, world: IWorld) {
		TODO("not implemented yet")
	}

	override fun onRemove(world: IWorld) {
		stateH.unregister()
		stateV.unregister()
		world.updateNeighboursOf(pos)
	}

	override fun getIdleColor(): Int = 0x757575

	override fun getActiveColor(): Int = 0x9E9E9E

	override fun getColor(): Int = if(stateH.isActiveD() || stateV.isActiveD()) getActiveColor() else getIdleColor()

	override fun setState(state: State, axis: Axis) {}

	override fun getState(axis: Axis): State = if(axis == Axis.HORIZONTAL) stateH else stateV
}