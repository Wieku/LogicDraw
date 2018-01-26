package me.wieku.circuits.world.elements.gates

import me.wieku.circuits.api.math.Vector2i

class XorGate(pos: Vector2i): SaveableGate(pos) {

	override fun update(tick: Long) {
		var calc = inputs.isXORActive()

		state!!.setActiveU(calc)
		setOut(calc)
	}

	override fun getIdleColor(): Int = 0xC51162

	override fun getActiveColor(): Int = 0xF50057

}