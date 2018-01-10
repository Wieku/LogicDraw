package me.wieku.circuits.world

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import me.wieku.circuits.api.element.IElement
import me.wieku.circuits.api.element.edit.Copyable
import me.wieku.circuits.api.math.*

class WorldClipboard(val objects: Array2D<IElement?>, val data: Array2D<HashMap<String, Any>?>? = null) {
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
		return WorldClipboard(objects.rotateLeft(), data?.rotateLeft())
	}

	fun rotateRight(): WorldClipboard {
		return WorldClipboard(objects.rotateRight(), data?.rotateRight())
	}

	fun flipHorizontal(): WorldClipboard {
		return WorldClipboard(objects.flipHorizontal(), data?.flipHorizontal())
	}

	fun flipVertical(): WorldClipboard {
		return WorldClipboard(objects.flipVertical(), data?.flipVertical())
	}

	companion object {
		fun create(selection: Rectangle, world: ClassicWorld): WorldClipboard {
			var map = Array2D<IElement?>(selection.width, selection.height)
			var data = Array2D<HashMap<String, Any>?>(selection.width, selection.height)
			for(x in 0 until selection.width) {
				for (y in 0 until selection.height) {
					val element = world[selection.x+x, selection.y+y]
					map[x][y] = element
					if(element is Copyable) {
						data[x][y] = element.copyData()
					}
				}
			}
			return WorldClipboard(map, data)
		}
	}


}