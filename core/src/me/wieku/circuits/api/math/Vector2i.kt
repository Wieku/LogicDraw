package me.wieku.circuits.api.math

import com.badlogic.gdx.math.MathUtils

class Vector2i(var x: Int, var y: Int) {

	constructor() : this(0, 0)

	constructor(old: Vector2i) : this(old.x, old.y)

	fun set(x: Int, y: Int): Vector2i {
		this.x = x
		this.y = y
		return this
	}

	fun set(vector: Vector2i) = set(vector.x, vector.y)

	fun add(vec: Vector2i) = add(vec.x, vec.y)

	fun add(num: Int) = add(num, num)

	fun add(x: Int, y: Int) = set(this.x + x, this.y + y)

	fun sub(vec: Vector2i) = sub(vec.x, vec.y)

	fun sub(num: Int) = sub(num, num)

	fun sub(x: Int, y: Int) = set(this.x - x, this.y - y)

	private fun signum(value: Int): Int = if(value < 0) -1 else if(value > 0) 1 else 0

	fun sig(): Vector2i = set(signum(x), signum(y))

	fun scl(amount: Float) = set((x * amount).toInt(), (y * amount).toInt())

	fun scl(vec: Vector2i) = scl(vec.x, vec.y)

	fun scl(x: Int, y: Int) = set(this.x * x, this.y * y)

	fun dst(vec: Vector2i) = dst(vec.x, vec.y)

	fun dst(x: Int, y: Int) = length(x - this.x, y - this.y)

	fun dot(vec: Vector2i) = dot(vec.x, vec.y)

	fun dot(x: Int, y: Int) = this.x * x + this.y * y

	private fun length(x: Int, y: Int) = Math.sqrt((x * x + y * y).toDouble()).toInt()

	fun len(): Int = length(this.x, this.y)

	fun angle(): Float {
		var angle = Math.atan2(y.toDouble(), x.toDouble()).toFloat() * MathUtils.radiansToDegrees
		if (angle < 0) angle += 360f
		return angle
	}

	fun clamp(boundX0: Int, boundY0:Int, boundX1: Int, boundY1: Int): Vector2i = set(clamp(x, boundX0, boundX1), clamp(y, boundY0, boundY1))

	private fun clamp(value: Int, min: Int, max: Int) = Math.max(min, Math.min(max, value))

	fun isInBounds(boundX0: Int, boundY0:Int, boundX1: Int, boundY1: Int) = x >= boundX0 && y >= boundY0 && x <= boundX1 && y <= boundY1

	fun copy() = Vector2i(this)

	fun clr() = set(0, 0)

	operator fun plus(vec: Vector2i) = Vector2i(x + vec.x, y + vec.y)

	operator fun plusAssign(vec: Vector2i) { add(vec) }

	operator fun minus(vec: Vector2i) = Vector2i(x - vec.x, y - vec.y)

	operator fun minusAssign(vec: Vector2i) { sub(vec) }

	operator fun times(vec: Vector2i): Int = dot(vec)

	operator fun not() = Vector2i(-x, -y)

	override fun hashCode() = 31 * x + y

	override fun toString() = "[$x, $y]"

	override fun equals(other: Any?): Boolean = other is Vector2i && other.x == this.x && other.y == this.y

}
