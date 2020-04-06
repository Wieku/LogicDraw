package me.wieku.circuits.api.element.holders

import me.wieku.circuits.api.collections.Array
//import me.wieku.circuits.api.element.BasicInput
import me.wieku.circuits.api.element.input.IInput

class Inputs(private val maxSize: Int) {

    constructor() : this(4)

    val array = Array<IInput?>(maxSize)
    var size = 0

    fun isActive(): Boolean {
        var calc = false
        for (i in 0 until size)
            calc = calc || array[i]!!.isActive()
        return calc
    }

    fun isAllActive(): Boolean {
        var calc = size > 0
        for (i in 0 until size)
            calc = calc && array[i]!!.isActive()
        return calc
    }

    fun isXORActive(): Boolean {
        var calc = false
        for (i in 0 until size)
            calc = calc xor array[i]!!.isActive()
        return calc
    }

    operator fun plusAssign(input: IInput) {
        if (size >= maxSize) error("Registered too many inputs!")
        array[size++] = input
    }

    fun clear() {
        size = 0
        array.fill(null)
    }

}