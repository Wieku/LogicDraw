package me.wieku.circuits.api.element

import me.wieku.circuits.api.state.State
import me.wieku.circuits.api.world.IWorld
import me.wieku.circuits.api.math.Vector2i

interface IElement {

	fun getIdleColor(): Int
	fun getActiveColor(): Int

	fun setState(state: State)
	fun getState(): State

	fun onPlace(world: IWorld)
	fun onNeighbourChange(world: IWorld, pos: Vector2i)
}