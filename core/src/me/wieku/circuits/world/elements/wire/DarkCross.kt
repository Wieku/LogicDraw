package me.wieku.circuits.world.elements.wire

import me.wieku.circuits.api.math.Vector2i

class DarkCross(pos: Vector2i): Cross(pos) {

	override fun getIdleColor(): Int = 0x131313

	override fun getActiveColor(): Int = 0x171717

}