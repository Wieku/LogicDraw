package me.wieku.circuits.world.elements.gates

import me.wieku.circuits.api.math.Vector2i

class NorGate(pos: Vector2i): SaveableGate(pos) {

	override fun update(tick: Long) {
		var calc = true
		for(i in 0 until inputs.size)
			calc = calc && !inputs[i].isActive()

		state.setActive(calc)
		setOut(calc)
	}

	override fun getIdleColor(): Int = 0xFFD600

	override fun getActiveColor(): Int = 0xFFEA00

	override fun getColor(): Int = if (state.isActiveD()) getActiveColor() else getIdleColor()

}