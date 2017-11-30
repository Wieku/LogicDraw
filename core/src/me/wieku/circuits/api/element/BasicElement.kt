package me.wieku.circuits.api.element

import me.wieku.circuits.api.math.Vector2i

abstract class BasicElement(val pos: Vector2i): IElement {

	override fun getPosition(): Vector2i = pos
}