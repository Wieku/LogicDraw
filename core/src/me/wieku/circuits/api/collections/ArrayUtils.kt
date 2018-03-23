package me.wieku.circuits.api.collections

typealias Array2D<T> = Array<Array<T>>

inline fun <reified T> Array2D(width: Int, height: Int) = Array(width) { Array<T?>(height)}

val <T> Array2D<T>.width: Int
	get() = size

val <T> Array2D<T>.height: Int
	get() = get(0).size

inline fun <reified T:Any?> Array(size: Int) = kotlin.Array<T?>(size, {null})

inline fun <reified T:Any?> Array<Array<T?>>.rotateRight(): Array<Array<T?>> {
	val width = size
	val height = get(0).size
	val map = Array2D<T?>(height, width)
	for(y in 0 until height) {
		for (x in 0 until width) {
			map[height-1-y][x] = get(x)[y]
		}
	}
	return map
}

inline fun <reified T:Any?> Array<Array<T?>>.rotateLeft(): Array<Array<T?>> {
	val width = size
	val height = get(0).size
	val map = Array2D<T?>(height, width)
	for(y in 0 until height) {
		for (x in 0 until width) {
			map[y][width-1-x] = get(x)[y]
		}
	}
	return map
}

inline fun <reified T:Any?> Array<Array<T?>>.flipHorizontal(): Array<Array<T?>> {
	val width = size
	val height = get(0).size
	val map = Array2D<T?>(width, height)
	for(y in 0 until height) {
		for (x in 0 until width) {
			map[width-1-x][y] = get(x)[y]
		}
	}
	return map
}

inline fun <reified T:Any?> Array<Array<T?>>.flipVertical(): Array<Array<T?>> {
	val width = size
	val height = get(0).size
	val map = Array2D<T?>(width, height)
	for (x in 0 until width) {
		for(y in 0 until height) {
			map[x][height-1-y] = get(x)[y]
		}
	}
	return map
}