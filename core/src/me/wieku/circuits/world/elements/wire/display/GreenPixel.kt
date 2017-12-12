package me.wieku.circuits.world.elements.wire.display

import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.world.elements.wire.Wire

class GreenPixel(pos: Vector2i): Wire(pos) {

	override fun getIdleColor(): Int = 0x121512

	override fun getActiveColor(): Int = 0x19FE19

}