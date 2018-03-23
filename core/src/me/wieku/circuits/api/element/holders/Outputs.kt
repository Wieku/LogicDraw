package me.wieku.circuits.api.element.holders

import me.wieku.circuits.api.collections.Array
import me.wieku.circuits.api.state.State

class Outputs (private val maxSize: Int) {

	constructor(): this(4)

	val array = Array<State?>(maxSize)
	var size = 0

	operator fun plusAssign(output: State) {
		if(size >= maxSize) error("Too many outputs registered!")
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