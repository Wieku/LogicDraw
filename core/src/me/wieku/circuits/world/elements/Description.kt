package me.wieku.circuits.world.elements

import me.wieku.circuits.api.element.BasicElement
import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.state.State
import me.wieku.circuits.api.world.IWorld

class Description(pos: Vector2i): BasicElement(pos) {

	override fun onPlace(world: IWorld) {}

	override fun onRemove(world: IWorld) {}

	override fun onNeighbourChange(position: Vector2i, world: IWorld) {}

	override fun getIdleColor(): Int = 0x8D6E63

	override fun getActiveColor(): Int = 0x8D6E63

	override fun getColor(): Int = 0x8D6E63

	override fun setState(state: State, axis: Axis) {}

	override fun getState(axis: Axis): State? = null

}