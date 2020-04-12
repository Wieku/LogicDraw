package me.wieku.circuits.api.state

import java.util.*

open class StateManager(private val managerSize: Int) {
	var input = IntArray(managerSize)
	var output = IntArray(managerSize)
	var children = Array<State?>(managerSize){null}

	protected val indexPool: Queue<Int> = ArrayDeque<Int>()
	protected var lastIndex = 0

	var usedNodes = 0
	protected set

	protected val stateQueue = ArrayDeque<State>(managerSize / 8)

	open fun free(index: Int) {
		input[index] = 0
		output[index] = 0
		children[index] = null
		if (index == lastIndex - 1) {
			var subIndex = index
			while (subIndex >= 0 && children[subIndex] == null) {
				indexPool.remove(subIndex)
				subIndex--
			}
			lastIndex = subIndex + 1
		} else {
			indexPool.add(index)
		}
	}

	open fun swap() {
		while (stateQueue.isNotEmpty()) {
			val state = stateQueue.poll()
			state.alreadyMarked = false
			input[state.id] = output[state.id]
		}

		usedNodes = lastIndex - indexPool.size
	}

	fun createState(): State {
		var state = if(indexPool.isEmpty()) {
			State(lastIndex++, this)
		} else {
			State(indexPool.poll(), this)
		}
		children[state.id] = state
		return state
	}

	fun markForUpdate(state: State) {
		stateQueue.push(state)
		state.alreadyMarked = true
	}

	operator fun get(index: Int) = input[index] > 0

	fun getDirty(index: Int) = output[index] > 0

	fun getState(index: Int) = children[index]

	operator fun set(index: Int, value: Int) {
		output[index] = value
	}
}