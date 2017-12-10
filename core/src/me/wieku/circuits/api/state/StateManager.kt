package me.wieku.circuits.api.state

import java.util.*

open class StateManager(private val managerSize: Int) {
	var input = ByteArray(managerSize)
	var output = ByteArray(managerSize)
	var used = ByteArray(managerSize)
	var children = Array<State?>(managerSize){null}

	protected val indexPool: Queue<Int> = ArrayDeque<Int>()
	protected var lastIndex = 0
	var usedNodes = 0
	protected set

	fun free(index: Int) {
		input[index] = 0
		output[index] = 0
		children[index] = null
		indexPool.add(index)
	}

	fun swap() {
		System.arraycopy(output, 0, input, 0, lastIndex)
		bytefill(used,  lastIndex, 0)
		usedNodes = lastIndex-indexPool.size
	}

	operator fun invoke(): State {
		var state = if(indexPool.isEmpty()) {
			State(lastIndex++, this)
		} else {
			State(indexPool.poll(), this)
		}
		children[state.id] = state
		return state
	}

	operator fun get(index: Int) = input[index] > 0

	fun getDirty(index: Int) = output[index] > 0

	fun getState(index: Int) = children[index]

	operator fun set(index: Int, value: Boolean) {
		output[index] = if(value) 1 else 0
	}

	private fun bytefill(array: ByteArray, length: Int, value: Byte) {
		array[0] = value

		var i = 1
		while(i < length) {
			System.arraycopy(array, 0, array, i, if((length - i) < i) length - i else i)
			i*=2
		}
	}
}