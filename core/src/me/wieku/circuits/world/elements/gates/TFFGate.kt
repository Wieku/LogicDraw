package me.wieku.circuits.world.elements.gates

import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.save.SaveManager
import me.wieku.circuits.world.ClassicWorld

class TFFGate(pos: Vector2i): SaveableGate(pos) {

	private var toUpdate = true

	override fun update(tick: Long) {
		var calc = false
		for(i in 0 until inputs.size)
			calc = calc || inputs[i].isActive()

		if(calc) {
			if(toUpdate) {
				state.setActive(!state.isActive())
				toUpdate = false
			}
		} else {
			toUpdate = true
		}

		setOut(state.isActive())
	}

	override fun getIdleColor(): Int = 0x311B92

	override fun getActiveColor(): Int = 0x4527A0

	override fun getColor(): Int = if (state.isActiveD()) getActiveColor() else getIdleColor()

	override fun load(world: ClassicWorld, manager: SaveManager) {
		super.load(world, manager)
		toUpdate = manager.getByte() == 1.toByte()
	}

	override fun save(manager: SaveManager) {
		super.save(manager)
		manager.putByte(if(toUpdate) 1 else 0)
	}
}