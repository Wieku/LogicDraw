package me.wieku.circuits.api.state

open class State(private val id: Int, private val manager: StateManager) {

	fun setActive(value: Boolean) { manager[id] = value }

	fun isActive() = manager[id]

	fun free() = manager.free(id)
}