package me.wieku.circuits.world.elements.gates

import me.wieku.circuits.api.math.Vector2i

class AndGate(pos: Vector2i): SaveableGate(pos) {

	override fun update(tick: Long) {
		var calc = inputs.isAllActive()

		state!!.setActiveU(calc)
		setOut(calc)
	}

	override fun getIdleColor(): Int = 0x00BFA5

	override fun getActiveColor(): Int = 0x1DE9B6

}