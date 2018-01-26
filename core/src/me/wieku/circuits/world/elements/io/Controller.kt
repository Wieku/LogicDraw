package me.wieku.circuits.world.elements.io

import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.save.Saveable

class Controller(pos: Vector2i): Input(pos), Saveable {

	override fun getIdleColor(): Int = 0x1B5E20

	override fun getActiveColor(): Int = 0x2E7D32

}