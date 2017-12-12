package me.wieku.circuits.world.elements.wire.display

import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.world.elements.wire.Wire

class WhitePixel(pos: Vector2i): Wire(pos) {

	override fun getIdleColor(): Int = 0x111111

	override fun getActiveColor(): Int = 0xFAFAFA

}