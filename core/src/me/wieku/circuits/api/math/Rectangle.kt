package me.wieku.circuits.api.math

class Rectangle() {
	var x = 0
	var y = 0
	var width = 0
	var height = 0

	constructor(from: Vector2i, to: Vector2i):this() {
		reshape(from, to)
	}

	fun reshape(from:Vector2i, to: Vector2i): Rectangle = reshape(from.x, from.y, to.x, to.y)

	fun reshape(x0: Int, y0: Int, x1: Int, y1: Int): Rectangle {
		x = Math.min(x0, x1)
		y = Math.min(y0, y1)
		width = Math.abs(x1-x0)
		height = Math.abs(y1-y0)
		return this
	}

}