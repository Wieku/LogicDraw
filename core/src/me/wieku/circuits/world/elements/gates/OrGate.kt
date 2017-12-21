package me.wieku.circuits.world.elements.gates

import me.wieku.circuits.api.math.Vector2i

class OrGate(pos: Vector2i): SaveableGate(pos) {

	override fun update(tick: Long) {
		var calc = false
		for(i in 0 until inputs.size)
			calc = calc || inputs[i].isActive()

		state!!.setActive(calc)
		setOut(calc)
	}

	override fun getIdleColor(): Int = 0xF57F17

	override fun getActiveColor(): Int = 0xF9A825

}