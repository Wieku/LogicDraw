package me.wieku.circuits.api.element.holders

import me.wieku.circuits.api.collections.Array
import me.wieku.circuits.api.element.BasicInput

class Inputs (private val maxSize: Int) {

	constructor(): this(4)

	val array = Array<BasicInput?>(maxSize)
	var size = 0

	operator fun plusAssign(input: BasicInput) {
		if(size >= maxSize) error("Registered too many inputs!")
		array[size++] = input
	}

	fun isActive() : Boolean {
		var calc = false
		for(i in 0 until size)
			calc = calc || array[i]!!.isActive()
		return calc
	}

	fun isAllActive() : Boolean {
		var calc = size > 0
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