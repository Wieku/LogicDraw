package me.wieku.circuits

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import me.wieku.circuits.api.world.clock.AsyncClock
import me.wieku.circuits.api.world.clock.Updatable

class Main : ApplicationAdapter(), Updatable.ByDelta {

	lateinit var ticker:AsyncClock
	var delt = 0f
	override fun update(value: Float) {
		delt+=value
		if(delt >=1f) {
			println(ticker.currentTPS)
			delt = 0f
		}
	}

	lateinit var batch: SpriteBatch
	lateinit var img: Texture

	override fun create() {
		batch = SpriteBatch()
		img = Texture("badlogic.jpg")

		ticker = AsyncClock(this, 100)
		ticker.start()
	}

	override fun render() {
		Gdx.gl.glClearColor(1f, 0f, 0f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

		batch.begin()
		batch.draw(img, 0f, 0f)
		batch.end()
	}

	override fun dispose() {
		img.dispose()
		batch.dispose()
	}

}