package me.wieku.circuits.render.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ExtendViewport
import me.wieku.circuits.render.scene.Label

class WorldCreator:Screen {

	private var stage: Stage = Stage(ExtendViewport(1024f, 768f))

	private var mainTable = Table()
	private var bannerTexture: Texture = Texture(Gdx.files.internal("assets/logo/banner_inv.png"), true)
	private var banner: Image = Image(bannerTexture)

	private var cell: Cell<Image>

	init {
		bannerTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear)
		banner.setScaling(Scaling.fillX)

		mainTable.top()
		cell = mainTable.add(banner).top().pad(20f).width(1024f*2/3).fill(true, false)
		mainTable.row()

		mainTable.add(Label("TEST", Color.WHITE, 100)).center().fillY().expandX()

		stage.addActor(mainTable)
		mainTable.setFillParent(true)
	}

	override fun show() {

	}

	override fun render(delta: Float) {
		Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
		mainTable.debug()

		cell.height(banner.imageHeight)
		mainTable.invalidate()

		stage.act(delta)
		stage.draw()
	}

	override fun resize(width: Int, height: Int) {
		stage.viewport.update(width, height, true)
		//mainTable.setBounds(0f, 0f, stage.width, stage.height)
	}

	override fun dispose() {
		stage.dispose()
	}

	override fun pause() {}

	override fun resume() {}

	override fun hide() {}
}