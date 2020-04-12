package me.wieku.circuits.world.state

import me.wieku.circuits.api.state.State
import me.wieku.circuits.api.state.StateManager
import me.wieku.circuits.render.map.WorldRenderer
import me.wieku.circuits.save.SaveManager
class ClassicStateManager(managerSize: Int): StateManager(managerSize) {

	var worldRenderer: WorldRenderer? = null
	set(value) {
		if (value != null) {
			children.forEach {
				if (it != null) {
					value.setStateData(it.id, it.isActive())
				}
			}
		}

		field = value
	}

	override fun free(index: Int) {
		super.free(index)
		worldRenderer?.setStateData(index, false)
	}

	override fun swap() {
		while (stateQueue.isNotEmpty()) {
			val state = stateQueue.poll()
			state.alreadyMarked = false
			val pre = input[state.id] > 0
			val result = output[state.id]
			input[state.id] = result
			val post = result > 0
			if (pre != post) {
				worldRenderer?.setStateData(state.id, post)
			}
		}

		usedNodes = lastIndex - indexPool.size
	}

	fun load(manager: SaveManager) {
		lastIndex = manager.getInteger()
		var poolsize = manager.getInteger()
		for(i in 0 until poolsize) {
			indexPool.add(manager.getInteger())
		}

		for(i in 0 until lastIndex) {
			input[i] = manager.getByte().toInt()
			output[i] = input[i]
			if(!indexPool.contains(i)) {
				children[i] = State(i, this)
			}
		}
		usedNodes = lastIndex - indexPool.size
	}

	fun save(manager: SaveManager) {
		manager.putInteger(lastIndex)
		manager.putInteger(indexPool.size)
		for(i in 0 until indexPool.size) {
			manager.putInteger(indexPool.poll())
		}

		for(i in 0 until lastIndex) {
			manager.putInteger(input[i])
		}
	}

}