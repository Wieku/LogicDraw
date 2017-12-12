package me.wieku.circuits.world.elements.wire

import me.wieku.circuits.api.math.Vector2i

class DarkWire(pos: Vector2i): Wire(pos) {

	override fun getIdleColor(): Int = 0x130000

	override fun getActiveColor(): Int = 0x200000

}