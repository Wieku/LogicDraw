package me.wieku.circuits.api.world

import me.wieku.circuits.api.element.BasicElement
import me.wieku.circuits.api.state.StateManager

interface IWorld {


	fun update()
	fun updateNeighboursOf(pos: Vector2i)
	fun getStateManager() : StateManager
}