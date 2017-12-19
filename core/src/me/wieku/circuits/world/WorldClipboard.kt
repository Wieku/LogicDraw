package me.wieku.circuits.world

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import me.wieku.circuits.api.element.IElement
import me.wieku.circuits.api.math.Rectangle
import me.wieku.circuits.api.math.Vector2i

class WorldClipboard(/*selection: Rectangle, world: ClassicWorld*/val objects: Array<Array<IElement?>>) {
	val width = objects.size
	val height = objects[0].size

	private var color = Color()
	private var location = Vector2i()
	fun drawClipboard(loc: Vector2i, renderer: ShapeRenderer) {
		renderer.setColor(0.1f, 0.1f, 0.1f, 0.5f)
		location.set(loc).sub(width/2, height/2)
		renderer.rect(location.x.toFloat(), location.y.toFloat(), width.toFloat(), height.toFloat())
		for(x in 0 until width) {
			for(y in 0 until height) {
				val el = objects[x][y]
				if(el != null) {
					color.set((el.getIdleColor().shl(8)) + 0x7F)
					renderer.color = color
					renderer.rect((x+location.x).toFloat(), (y+location.y).toFloat(), 1f, 1f)
				}
			}
		}
	}

	operator fun get(x:Int, y:Int) = objects[x][y]

	fun rotateLeft(): WorldClipboard {
		var map = Array(height) { Array<IElement?>(width) {null}}
		for(y in 0 until height) {
			for (x in 0 until width) {
				map[y][width-1-x] = objects[x][y]
			}
		}
		return WorldClipboard(map)
	}

	fun rotateRight(): WorldClipboard {
		var map = Array(height) { Array<IElement?>(width) {null}}
		for(y in 0 until height) {
			for (x in 0 until width) {
				map[height-1-y][x] = objects[x][y]
			}
		}
		return WorldClipboard(map)
	}

	fun flipHorizontal(): WorldClipboard {
		var map = Array(width) { Array<IElement?>(height) {null}}
		for(y in 0 until height) {
			for (x in 0 until width) {
				map[width-1-x][y] = objects[x][y]
			}
		}
		return WorldClipboard(map)
	}

	fun flipVertical(): WorldClipboard {
		var map = Array(width) { Array<IElement?>(height) {null}}
		for (x in 0 until width) {
			for(y in 0 until height) {
				map[x][height-1-y] = objects[x][y]
			}
		}
		return WorldClipboard(map)
	}

	companion object {
		fun create(selection: Rectangle, world: ClassicWorld): WorldClipboard {
			var map = Array(selection.width) { Array<IElement?>(selection.height) {null}}
			for(x in 0 until selection.width) {
				for (y in 0 until selection.height) {
					map[x][y] = world[selection.x+x, selection.y+y]
				}
			}
			return WorldClipboard(map)
		}
	}


}