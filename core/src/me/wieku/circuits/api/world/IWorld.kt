package me.wieku.circuits.api.world

import me.wieku.circuits.api.element.BasicElement
import me.wieku.circuits.api.state.StateManager
import me.wieku.circuits.utils.math.Vector2i

interface IWorld {


	fun update()
	fun updateNeighboursOf(pos: Vector2i)
	fun getStateManager() : StateManager
}