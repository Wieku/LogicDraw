package me.wieku.circuits.api.element.holders

import me.wieku.circuits.api.state.State

class Outputs {

	val array = Array<State?>(4) { null }
	var size = 0

	operator fun plusAssign(output: State) {
		if(size >= 4) error("Registered too many outputs!")
		array[size++] = output
	}

	fun setActive(value: Boolean) {
		for(i in 0 until size)
			array[i]!!.setActive(value)
	}

	fun clear() {
		size = 0
		array.fill(null)
	}

}