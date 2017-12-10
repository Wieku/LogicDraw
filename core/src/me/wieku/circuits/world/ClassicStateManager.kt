package me.wieku.circuits.world

import me.wieku.circuits.api.state.State
import me.wieku.circuits.api.state.StateManager
import me.wieku.circuits.save.SaveManager

class ClassicStateManager(managerSize: Int): StateManager(managerSize) {


	fun load(manager: SaveManager) {
		lastIndex = manager.getInteger()
		var poolsize = manager.getInteger()
		for(i in 0 until poolsize) {
			indexPool.add(manager.getInteger())
		}

		for(i in 0 until lastIndex) {
			input[i] = manager.getByte()
			output[i] = input[i]
			if(!indexPool.contains(i)) {
				children[i] = State(i, this)
			}
		}
	}

	fun save(manager: SaveManager) {
		manager.putInteger(lastIndex)
		manager.putInteger(indexPool.size)
		for(i in 0 until indexPool.size) {
			manager.putInteger(indexPool.poll())
		}

		for(i in 0 until lastIndex) {
			manager.putByte(input[i])
		}
	}

}