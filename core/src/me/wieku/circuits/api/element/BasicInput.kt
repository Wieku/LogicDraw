package me.wieku.circuits.api.element

import me.wieku.circuits.api.math.Vector2i

abstract class BasicInput(pos: Vector2i): BasicElement(pos) {
	abstract fun isActive(): Boolean
}