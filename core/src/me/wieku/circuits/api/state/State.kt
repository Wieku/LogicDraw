package me.wieku.circuits.api.state

open class State(private val id: Int, private val manager: StateManager) {

	private var holders: Int = 0

	fun setActive(value: Boolean) { manager[id] = value }

	fun isActive() = manager[id]

	fun isActiveD() = manager.getDirty(id)

	fun free() = manager.free(id)

	fun register() {
		++holders
	}

	fun unregister() {
		--holders
		if(holders <= 0) free()
	}

	fun finalize() {
		free()
	}
}