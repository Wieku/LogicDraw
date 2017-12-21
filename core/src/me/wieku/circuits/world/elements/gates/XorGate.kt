package me.wieku.circuits.world.elements.gates

import me.wieku.circuits.api.math.Vector2i

class XorGate(pos: Vector2i): SaveableGate(pos) {

	override fun update(tick: Long) {
		var calc = false
		for(i in 0 until inputs.size)
			calc = calc.xor(inputs[i].isActive())

		state!!.setActive(calc)
		setOut(calc)
	}

	override fun getIdleColor(): Int = 0xC51162

	override fun getActiveColor(): Int = 0xF50057

}