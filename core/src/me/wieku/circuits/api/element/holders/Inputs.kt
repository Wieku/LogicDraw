package me.wieku.circuits.api.element.holders

import me.wieku.circuits.api.element.BasicInput

class Inputs {

	val array = Array<BasicInput?>(4) { null }
	var size = 0

	operator fun plusAssign(input: BasicInput) {
		if(size >= 4) error("Registered too many size!")
		array[size++] = input
	}

	fun isActive() : Boolean {
		var calc = false
		for(i in 0 until size)
			calc = calc || array[i]!!.isActive()
		return calc
	}

	fun isAllActive() : Boolean {
		var calc = true
		for(i in 0 until size)
			calc = calc && array[i]!!.isActive()
		return calc
	}

	fun isXORActive() : Boolean {
		var calc = false
		for(i in 0 until size)
			calc = calc xor array[i]!!.isActive()
		return calc
	}

	fun clear() {
		size = 0
		array.fill(null)
	}

}