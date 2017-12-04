package me.wieku.circuits

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector3
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
			//println(value-tick)
			tick = value
			time = System.nanoTime()
		}
	}

	lateinit var renderer: ShapeRenderer
	lateinit var camera:OrthographicCamera

	override fun create() {
		renderer = ShapeRenderer()
		camera = OrthographicCamera(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

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

		ticker = AsyncClock(this, 1000000000)
		ticker.start()

		Gdx.input.inputProcessor = object: InputAdapter(){
			override fun scrolled(amount: Int): Boolean {
				if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
					var before = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
					camera.zoom = Math.max(0.01f, Math.min(4f, camera.zoom+amount*camera.zoom*0.1f))
					camera.update()
					var after = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
					camera.position.sub(after.sub(before))
					camera.position.set(Math.max(0f, Math.min(10*40f, camera.position.x)), Math.max(0f, Math.min(10*40f, camera.position.y)), 0f)
				}
				return super.scrolled(amount)
			}

			override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
				if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
					camera.position.sub(Gdx.input.getDeltaX(pointer).toFloat()*camera.zoom, Gdx.input.getDeltaY(pointer).toFloat()*camera.zoom, 0f)
					camera.position.set(Math.max(0f, Math.min(10*40f, camera.position.x)), Math.max(0f, Math.min(10*40f, camera.position.y)), 0f)
				}
				return super.touchDragged(screenX, screenY, pointer)
			}
		}

	}

	var color = Color()
	override fun render() {
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

		camera.update()
		renderer.projectionMatrix = camera.combined
		renderer.begin(ShapeRenderer.ShapeType.Filled)
		renderer.color = Color.BLACK
		renderer.rect(0f, 0f, 10*40f, 10*40f)
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

	override fun resize(width: Int, height: Int) {
		super.resize(width, height)
		camera.setToOrtho(true, width.toFloat(), height.toFloat())
	}

	override fun dispose() {
		renderer.dispose()
	}

}