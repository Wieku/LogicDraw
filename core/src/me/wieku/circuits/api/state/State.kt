package me.wieku.circuits.api.state

open class State(val id: Int, private val manager: StateManager) {

	var holders: Int = 0
	private set

	private var destroyed = false

	fun setActive(value: Boolean) {
		if(manager.used[id] > 0 && !value) return
		manager[id] = value
		manager.used[id] = 1
	}

	fun isActive() = manager[id]

	fun isActiveD() = manager.getDirty(id)

	fun free() {
		if(!destroyed)
			manager.free(id)
		destroyed = true
	}

	fun register() {
		++holders
	}

	fun unregister() {
		--holders
		if(holders <= 0) {
			free()
		}
	}

	@Suppress("UNUSED")
	fun finalize() {
		free()
	}
}