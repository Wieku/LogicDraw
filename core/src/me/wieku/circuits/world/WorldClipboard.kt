package me.wieku.circuits.world

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import me.wieku.circuits.api.element.IElement
import me.wieku.circuits.api.math.Rectangle
import me.wieku.circuits.api.math.Vector2i

class WorldClipboard(val selection: Rectangle, world: ClassicWorld) {
	private val submap: Array<Array<IElement?>> = Array(selection.width) { Array<IElement?>(selection.height) {null} }

	init {
		println(selection.x.toString() + " " + selection.width.toString())
		for(x in 0 until selection.width) {
			for (y in 0 until selection.height) {
				submap[x][y] = world[selection.x+x, selection.y+y]
			}
		}
	}

	private var color = Color()
	private var location = Vector2i()
	fun drawClipboard(loc: Vector2i, renderer: ShapeRenderer) {
		renderer.setColor(0.1f, 0.1f, 0.1f, 0.5f)
		location.set(loc).sub(selection.width/2, selection.height/2)
		renderer.rect(location.x.toFloat(), location.y.toFloat(), selection.width.toFloat(), selection.height.toFloat())
		for(x in 0 until selection.width) {
			for(y in 0 until selection.height) {
				val el = submap[x][y]
				if(el != null) {
					color.set((el.getIdleColor().shl(8)) + 0x7F)
					renderer.color = color
					renderer.rect((x+location.x).toFloat(), (y+location.y).toFloat(), 1f, 1f)
				}
			}
		}
	}

	operator fun get(x:Int, y:Int) = submap[x][y]

}