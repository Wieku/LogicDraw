package me.wieku.circuits.api.world

import me.wieku.circuits.api.state.StateManager
import me.wieku.circuits.api.math.Vector2i

interface IWorld {

	fun update(tick: Long)
	fun updateNeighboursOf(pos: Vector2i)
	fun getStateManager() : StateManager
}