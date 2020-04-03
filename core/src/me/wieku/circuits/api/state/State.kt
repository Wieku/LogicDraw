package me.wieku.circuits.api.state

open class State(val id: Int, private val manager: StateManager) {

	var holders: Int = 0
	private set

	var activeNum: Int
		get() = manager.input[id]
		set(value) {
			manager.output[id] = value
		}

	private var destroyed = false

	fun setActive(value: Boolean) {
		if (value) {
			manager.output[id]++
		} else if (manager.output[id] > 0) {
			manager.output[id]--
		}
	}

	fun high() {
		manager.output[id]++
	}

	fun low() {
		manager.output[id]--
	}

	fun setActiveU(value: Boolean) {
		if (value != manager.getDirty(id)) {
			if (value) {
				high()
			} else {
				low()
			}
		}
	}

	fun isActive() = manager[id]

	fun numActive() = manager.input[id]

	fun isActiveD() = manager.getDirty(id)

	fun free() {
		if(!destroyed)
			manager.free(id)
		destroyed = true
	}

	fun register() {
		++holders
	}

	fun unregister(wasActive: Boolean = false) {
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