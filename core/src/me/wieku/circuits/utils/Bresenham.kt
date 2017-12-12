package me.wieku.circuits.utils

import com.badlogic.gdx.math.Bresenham2
import me.wieku.circuits.api.math.Vector2i

object Bresenham {
	private val bresenham = Bresenham2()

	fun convert(from: Vector2i, to: Vector2i) : Array<Vector2i> {
		var array1 = bresenham.line(from.x, from.y, to.x, to.y)
		var array2 = Array(array1.size) { Vector2i() }
		for(i in 0 until array1.size) {
			array2[i].set(array1[i].x, array1[i].y)
		}
		return array2
	}

	fun iterate(from: Vector2i, to: Vector2i, consumer: (Vector2i) -> Unit) {
		var array1 = bresenham.line(from.x, from.y, to.x, to.y)
		array1.forEach {
			consumer.invoke(Vector2i(it.x, it.y))
		}
	}

	private val temp = Vector2i()
	fun iterateFast(from: Vector2i, to: Vector2i, consumer: (Vector2i) -> Unit) {
		var array1 = bresenham.line(from.x, from.y, to.x, to.y)
		array1.forEach {
			consumer.invoke(temp.set(it.x, it.y))
		}
	}

}