package me.wieku.circuits.api.state

import me.wieku.circuits.api.element.input.IInput

open class State(val id: Int, private val manager: StateManager) {

	var holders: Int = 0
	private set

	private var inputGates = ArrayList<IInput>()

	var activeNum: Int
		get() = manager.input[id]
		set(value) {
			manager.output[id] = value
		}

	private var destroyed = false

	fun setActive(value: Boolean) {
		if (destroyed) return

		if (value) {
			high()
		} else if (manager.getDirty(id)) {
			low()
		}

		if (isActive() != isActiveD()) {
			for (i in inputGates.indices) {
				val tickables = inputGates[i].getGates()
				for (j in tickables.indices) {
					tickables[j].markForUpdate()
				}
			}
		}
	}

	fun high() {
		manager.output[id]++
	}

	fun low() {
		manager.output[id]--
	}

	fun setActiveU(value: Boolean) {
		if (destroyed) return

		if (value != manager.getDirty(id)) {
			if (value) {
				high()
			} else {
				low()
			}
		}
	}

	fun isActive() = manager[id]

	fun isActiveD() = manager.getDirty(id)

	fun free() {
		if(!destroyed) {
			manager.free(id)
			inputGates.clear()
		}
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

	fun addInput(input: IInput) {
		inputGates.add(input)
		val tickables = input.getGates()
		for (j in tickables.indices) {
			tickables[j].markForUpdate()
		}
	}

	fun deleteInput(input: IInput) {
		inputGates.remove(input)
	}

	@Suppress("UNUSED")
	fun finalize() {
		free()
	}
}