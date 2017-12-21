package me.wieku.circuits.world.elements.gates

import me.wieku.circuits.api.math.Vector2i

class AndGate(pos: Vector2i): SaveableGate(pos) {

	override fun update(tick: Long) {
		var calc = inputs.size > 1
		for(i in 0 until inputs.size)
			calc = calc && inputs[i].isActive()

		state!!.setActive(calc)
		setOut(calc)
	}

	override fun getIdleColor(): Int = 0x00BFA5

	override fun getActiveColor(): Int = 0x1DE9B6

}