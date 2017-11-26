package me.wieku.circuits.api.math

class Vector2i(var x: Int, var y: Int) {

	constructor() : this(0, 0)

	constructor(old: Vector2i) : this(old.x, old.y)

	operator fun set(x: Int, y: Int): Vector2i {
		this.x = x
		this.y = y
		return this
	}


	fun set(vector: Vector2i): Vector2i {
		return set(vector.x, vector.y)
	}


	fun add(vec: Vector2i): Vector2i {
		return add(vec.x, vec.y)
	}


	fun add(x: Int, y: Int): Vector2i {
		return set(this.x + x, this.y + y)
	}


	fun sub(vec: Vector2i): Vector2i {
		return sub(vec.x, vec.y)
	}


	fun sub(x: Int, y: Int): Vector2i {
		return set(this.x - x, this.y - y)
	}

	private fun signum(value: Int): Int = if(value<0) -1 else (if(value > 0) 1 else 0)

	fun sig(): Vector2i = set(signum(x), signum(y))

	fun scl(amount: Float): Vector2i {
		return set((x * amount).toInt(), (y * amount).toInt())
	}


	fun scl(vec: Vector2i): Vector2i {
		return scl(vec.x, vec.y)
	}


	fun scl(x: Int, y: Int): Vector2i {
		return set(this.x * x, this.y * y)
	}

	fun dst(vec: Vector2i): Int {
		return dst(vec.x, vec.y)
	}


	fun dst(x: Int, y: Int): Int {
		val a = x - this.x
		val b = y - this.y
		return Math.sqrt((a * a + b * b).toDouble()).toInt()
	}


	fun dot(vec: Vector2i): Int {
		return dot(vec.x, vec.y)
	}


	fun dot(x: Int, y: Int): Int {
		return this.x * x + this.y * y
	}


	fun len(): Int {
		return Math.sqrt((x * x + y * y).toDouble()).toInt()
	}

	fun isInBounds(boundX0: Int, boundY0:Int, boundX1: Int, boundY1: Int) = x >= boundX0 && y >= boundY0 && x <= boundX1 && y <= boundY1

	fun copy(): Vector2i {
		return Vector2i(this)
	}


	override fun hashCode(): Int {
		return 31 * x + y
	}


	override fun toString(): String {
		return "[$x, $y]"
	}

	override fun equals(obj: Any?): Boolean {
		if (obj == null)
			return false
		if (obj !is Vector2i)
			return false
		// TODO: better checking
		val vec = obj as Vector2i?
		return (vec!!.x != this.x || vec.y != this.y )
	}


	fun clr(): Vector2i {
		return set(0, 0)
	}


	fun add(num: Int): Vector2i {
		return add(num, num)
	}


	fun sub(num: Int): Vector2i {
		return sub(num, num)
	}


	operator fun plus(vec: Vector2i) = Vector2i(x + vec.x, y + vec.y)

	operator fun plusAssign(vec: Vector2i) { add(vec) }

	operator fun minus(vec: Vector2i) = Vector2i(x - vec.x, y - vec.y)

	operator fun minusAssign(vec: Vector2i) { sub(vec) }

	operator fun times(vec: Vector2i): Int = dot(vec)

	operator fun not() = Vector2i(-x, -y)

}
