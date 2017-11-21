package me.wieku.circuits.api.math

enum class Direction(val x: Int, val y: Int, val axis: Axis) {
	NORTH(0, 1, Axis.VERTICAL),
	EAST(1, 0, Axis.HORIZONTAL),
	SOUTH(0, -1, Axis.VERTICAL),
	WEST(-1, 0, Axis.HORIZONTAL),
	UNKNOWN(0, 0, Axis.UNKNOWN);

	val asVector = Vector2i(x, y)

	fun opposite() = getFromOrdinal(OPPOSITES[ordinal])

	companion object {
		private val OPPOSITES: IntArray = intArrayOf(2, 3, 0, 1)
		private val MATRIX: Array<IntArray> = arrayOf(intArrayOf(4, 3, 4), intArrayOf(2, 4, 0), intArrayOf(4, 1, 4))
		private val VALID_DIRECTIONS: Array<Direction> = arrayOf(NORTH, EAST, SOUTH, WEST)
		private var tmpVector: Vector2i = Vector2i()

		private fun getFromOrdinal(id: Int): Direction = when (id) {in 0..VALID_DIRECTIONS.size -> VALID_DIRECTIONS[id] else -> UNKNOWN }

		fun getDirection(from: Vector2i, to: Vector2i): Direction {
			tmpVector.set(to).sub(from).sig()
			return getFromOrdinal(MATRIX[tmpVector.x+1][tmpVector.y+1])
		}
	}

}