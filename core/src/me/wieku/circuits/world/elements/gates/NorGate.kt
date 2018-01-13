package me.wieku.circuits.world.elements.gates

import me.wieku.circuits.api.math.Vector2i

class NorGate(pos: Vector2i): SaveableGate(pos) {

	override fun update(tick: Long) {
		var calc = inputs.isActive()

		state!!.setActive(!calc)
		setOut(!calc)
	}

	override fun getIdleColor(): Int = 0xFFD600

	override fun getActiveColor(): Int = 0xFFEA00

}