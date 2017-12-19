package me.wieku.circuits.utils

import com.badlogic.gdx.math.MathUtils

class Palette {

	val palette = arrayOfNulls<String>(9)

	var currentBrush: String? = null
	var current = 0
	private set

	fun back() {
		--current
		if(current < 0 ) current = palette.size - 1
		currentBrush = palette[current]
	}

	fun forward() {
		++current
		if(current >= palette.size ) current = 0
		currentBrush = palette[current]
	}

	fun select(index: Int = 0) {
		current = MathUtils.clamp(index, 0, 8)
		currentBrush = palette[current]
	}

	fun put(name: String) {
		if(palette[current] == null) {
			palette[current] = name
			currentBrush = name
			return
		}

		var firstNull = -1
		for(i in 0..8) {
			if(palette[i] == null && firstNull == -1) {
				firstNull = i
			} else if(palette[i] == name) {
				current = i
				currentBrush = name
				return
			}
		}
		if(firstNull == -1) {
			palette[current] = name
			currentBrush = name
		} else {
			current = firstNull
			currentBrush = name
			palette[current] = name
		}

	}

}