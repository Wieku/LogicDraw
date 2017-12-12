package me.wieku.circuits.world.elements.wire.display

import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.world.elements.wire.Wire

class RedPixel(pos: Vector2i): Wire(pos) {

	override fun getIdleColor(): Int = 0x1B1010

	override fun getActiveColor(): Int = 0xFE2626

}