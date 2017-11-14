package me.wieku.circuits.api.state

import java.util.*

class StateManager(private val managerSize: Int) {
	var input = ByteArray(managerSize)
	var output = ByteArray(managerSize)

	private val indexPool: Queue<Int> = ArrayDeque<Int>()
	private var lastIndex = 0

	fun free(index: Int) {
		indexPool.add(index)
		input[index] = 0
		output[index] = 0
	}

	fun merge() = System.arraycopy(output, 0, input, 0, managerSize)

	operator fun invoke(): State = if(indexPool.isEmpty()) State(lastIndex++, this) else State(indexPool.poll(), this)

	operator fun get(index: Int):Boolean = input[index] > 0

	operator fun set(index: Int, value: Boolean) {
		output[index] = if(value) 1 else 0
	}
}