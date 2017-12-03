package me.wieku.circuits

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.world.clock.AsyncClock
import me.wieku.circuits.api.world.clock.Updatable
import me.wieku.circuits.world.ClassicWorld

class Main : ApplicationAdapter(), Updatable.ByTick {

	lateinit var ticker:AsyncClock
	var world = ClassicWorld(10, 10)
	var time = System.nanoTime()
	var tick:Long = 0
	override fun update(value: Long) {
		world.update(value)
		if(System.nanoTime()-time >= 1000000000) {
			println(value-tick)
			tick = value
			time = System.nanoTime()
		}
	}

	lateinit var renderer: ShapeRenderer

	override fun create() {
		renderer = ShapeRenderer()

		world.placeElement(Vector2i(1, 5), "Wire")
		world.placeElement(Vector2i(2, 5), "Nor")
		world.placeElement(Vector2i(3, 5), "Input")
		world.placeElement(Vector2i(4, 5), "Wire")

		world.placeElement(Vector2i(1, 4), "Wire")
		world.placeElement(Vector2i(4, 4), "Nor")

		world.placeElement(Vector2i(1, 3), "Wire")
		world.placeElement(Vector2i(4, 3), "Input")

		world.placeElement(Vector2i(1, 2), "Wire")
		world.placeElement(Vector2i(4, 2), "Wire")

		world.placeElement(Vector2i(2, 2), "Input")
		world.placeElement(Vector2i(3, 2), "Nor")

		ticker = AsyncClock(this, 1000000)
		ticker.start()
	}

	var color = Color()
	override fun render() {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

		renderer.begin(ShapeRenderer.ShapeType.Filled)
		for(x in 0 until 10) {
			for(y in 0 until 10) {
				var el = world[x, y]
				if(el != null) {
					color.set((el.getColor().shl(8)) + 0xFF)
					renderer.color = color
					renderer.rect(x*40f, y*40f, 40f, 40f)
				}
			}
		}
		renderer.end()
	}

	override fun dispose() {
		renderer.dispose()
	}

}