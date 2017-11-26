package me.wieku.circuits.api.math

enum class Axis {

	HORIZONTAL,
	VERTICAL,
	UNKNOWN;

	companion object fun getAxis(from: Vector2i, to: Vector2i) = Direction.getDirection(from, to).axis

}