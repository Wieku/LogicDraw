package me.wieku.circuits.render.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ExtendViewport
import me.wieku.circuits.Main
import me.wieku.circuits.render.scene.Label
import me.wieku.circuits.render.scene.Table
import me.wieku.circuits.render.scene.getTextButtonStyle
import me.wieku.circuits.render.scene.getTextFieldStyle
import me.wieku.circuits.world.ClassicWorld
import java.io.File

class WorldCreator:Screen {

	private var stage: Stage = Stage(ExtendViewport(1024f, 768f))

	private var mainTable = Table()
	private var bannerTexture: Texture = Texture(Gdx.files.internal("assets/logo/banner_inv.png"), true)
	private var banner: Image = Image(bannerTexture)

	private var cell: Cell<Image>

	init {
		File("maps/").mkdir()
		bannerTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear)
		banner.setScaling(Scaling.fillX)

		mainTable.top()
		cell = mainTable.add(banner).top().pad(20f).width(1024f*2/3).fill(true, false)
		mainTable.row()

		var creatorTable = Table()

		creatorTable.add(Label("Create new world:", Color.WHITE, 24)).left().fillX().colspan(4).row()

		creatorTable.add(Label("World name:", Color.WHITE, 12)).left()
		var nameField = TextField("Test", getTextFieldStyle(Color(0.07f, 0.07f, 0.07f, 1f), Color.WHITE, 12))
		creatorTable.add(nameField).colspan(3).padBottom(2f).fillX().row()

		creatorTable.add(Label("World size:", Color.WHITE, 12)).left()

		var widthField = TextField("100", getTextFieldStyle(Color(0.07f, 0.07f, 0.07f, 1f), Color.WHITE, 12))
		var heightField = TextField("100", getTextFieldStyle(Color(0.07f, 0.07f, 0.07f, 1f), Color.WHITE, 12))

		var filter = TextField.TextFieldFilter { textField, c ->
			var accept = Character.getType(c) != Character.MATH_SYMBOL.toInt() && Character.isDigit(c)
			if(accept) {
				val txt = textField!!.text + c
				if(txt != MathUtils.clamp(txt.toInt(), 1, 4096).toString())
					accept = false
			}
			accept
		}
		var listener = object: ChangeListener() {
			override fun changed(event: ChangeEvent?, actor: Actor?) {
				if(widthField.text.isEmpty())
					event!!.cancel()
				if(heightField.text.isEmpty())
					event!!.cancel()

			}
		}

		widthField.textFieldFilter = filter
		widthField.addListener(listener)
		heightField.textFieldFilter = filter
		heightField.addListener(listener)

		creatorTable.add(widthField).left()
		creatorTable.add(Label("x", Color.WHITE, 12)).left()
		creatorTable.add(heightField).left().row()

		var createButton = TextButton("Create!", getTextButtonStyle(Color(0.1f, 0.1f, 0.1f, 1f), Color.WHITE, 14))
		createButton.addListener(object: ClickListener(){
			override fun clicked(event: InputEvent?, x: Float, y: Float) {
				super.clicked(event, x, y)
				Main.screen = Editor(ClassicWorld(widthField.text.toInt(), heightField.text.toInt(), nameField.text))
			}
		})
		creatorTable.add(createButton).colspan(5).fillX()

		mainTable.add(creatorTable).center().row()

		stage.addActor(mainTable)
		mainTable.setFillParent(true)
	}

	override fun show() {
		Gdx.input.inputProcessor = stage
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