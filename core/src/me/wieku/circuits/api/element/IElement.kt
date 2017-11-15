package me.wieku.circuits.api.element

import com.badlogic.gdx.graphics.Color
import me.wieku.circuits.api.state.State
import me.wieku.circuits.api.world.IWorld
import me.wieku.circuits.utils.math.Vector2i

interface IElement {

	fun getIdleColor(): Color
	fun getActiveColor(): Color

	fun setState(state: State)
	fun getState(): State

	fun onPlace(world: IWorld)
	fun onNeighbourChange(world: IWorld, pos: Vector2i)
}