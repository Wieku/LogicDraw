package me.wieku.circuits.render.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ExtendViewport
import me.wieku.circuits.Main
import me.wieku.circuits.world.ClassicWorld
import java.io.File
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.kotcrab.vis.ui.util.adapter.ArrayListAdapter
import com.kotcrab.vis.ui.widget.ListView
import com.kotcrab.vis.ui.widget.VisTable
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel
import com.kotcrab.vis.ui.widget.spinner.Spinner
import ktx.vis.table
import me.wieku.circuits.render.scene.*
import me.wieku.circuits.render.scene.actors.MenuMap
import me.wieku.circuits.save.SaveManagers
import me.wieku.circuits.utils.Version
import java.util.*
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.util.form.FormInputValidator
import com.kotcrab.vis.ui.util.form.SimpleFormValidator
import com.kotcrab.vis.ui.widget.toast.MessageToast
import ktx.vis.window


class WorldCreator:Screen {

	private var stage: Stage = Stage(ScreenViewport())

	private var mainTable = Table()
	private var bannerTexture: Texture = Texture(Gdx.files.internal("assets/logo/banner_inv.png"), true)
	private var banner: Image = Image(bannerTexture)

	//private var cell: Cell<Image>

	init {
		File("maps/").mkdir()
		File("blueprints/").mkdir()
		mainTable.top().left()
		bannerTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear)
		banner.setScaling(Scaling.fillX)
		/*


		mainTable.top()
		cell = mainTable.add(banner).top().pad(20f).width(1024f*2/3).fill(true, false)
		mainTable.row()

		var creatorTable = Table()

		creatorTable.add(Label("Create new world:", Color.WHITE, 24)).left().fillX().expandX().colspan(4).row()

		creatorTable.add(Label("World name:", Color.WHITE, 12)).left()
		var nameField = TextField("Test", getTextFieldStyle(Color(0.07f, 0.07f, 0.07f, 1f), Color.WHITE, 12))
		creatorTable.add(nameField).colspan(3).padBottom(2f).fillX().row()

		creatorTable.add(Label("World size:", Color.WHITE, 12)).left()

		val widthModel = IntSpinnerModel(1024, 100, 8192, 1)
		val heightModel = IntSpinnerModel(1024, 100, 8192, 1)

		var widthSpinner = Spinner(null, widthModel)
		var heightSpinner = Spinner(null, heightModel)

		creatorTable.add(widthSpinner).left()
		creatorTable.add(Label("x", Color.WHITE, 12)).left()
		creatorTable.add(heightSpinner).left().row()

		var createButton = TextButton("Create!", getTextButtonStyle(Color(0.1f, 0.1f, 0.1f, 1f), Color.WHITE, 14))
		createButton.addListener(object: ClickListener(){
			override fun clicked(event: InputEvent?, x: Float, y: Float) {
				super.clicked(event, x, y)
				val width = widthModel.value
				val height = heightModel.value
				Main.screen = Editor(ClassicWorld(width, height, nameField.text))
			}
		})
		creatorTable.add(createButton).colspan(4).fillX()

		mainTable.add(creatorTable)*//*.width(1024f*2/3)*//*.center().colspan(3).row()
		mainTable.add(Label("Load world:", Color.WHITE, 24)).left().colspan(3).row()

		var worldsTable = Table()

		for(file in File("maps/").listFiles()) {
			try {
				var arr = Array<String>(4) { ""}
				arr[0] = file.name
				var arr2 = SaveManagers.getHeader(file)
				for(i in 1..3) arr[i] = arr2[i-1]
				worldsTable.add(MenuMap(arr))*//*.width(1024f*2/3)*//*.fillX().row()
			} catch (e: Exception){
				e.printStackTrace()
			}
		}

		var pane = ScrollPane(worldsTable, getScrollPaneStyle(Color.BLACK, Color.WHITE))
		pane.setFadeScrollBars(false)
		pane.setSmoothScrolling(false)
		(pane.getChildren().get(0) as Table).top().left()
		pane.setCancelTouchFocus(true)
		pane.setScrollingDisabled(true, false)
		mainTable.add(pane)*//*.width(1024f * 2/3)*//*.fill().colspan(3)*/

		val adapter = object: ArrayListAdapter<String, VisTable>(File("maps/").listFiles().map { it.name }.toMutableList() as ArrayList<String>?) {
			private val bg = VisUI.getSkin().getDrawable("window-bg")
			private val selection = VisUI.getSkin().getDrawable("list-selection")

			init {
				selectionMode = SelectionMode.SINGLE
			}

			override fun createView(item: String?): VisTable {
				var arr2 = SaveManagers.getHeader(File("maps/$item"))
				return table {
					background = bg
					left()
					label(arr2[0]).cell(padBottom = 3f, align = Align.top)
					row()
					label("Size: ${arr2[1]}x${arr2[2]}").cell(align = Align.left, growX = true)
					row()
					label("File: $item").cell(align = Align.left, growX = true)
					pad(5f)
				}
			}

			override fun selectView(view: VisTable?) {
				view!!.setBackground(selection)
			}

			override fun deselectView(view: VisTable?) {
				view!!.setBackground(bg)
			}

		}

		val view = ListView<String>(adapter)

		view.mainTable.background = getTxRegion(Color(0.08f, 0.08f, 0.08f, 1f))

		view.setItemClickListener {
			var file = File("maps/$it")
			Main.screen = Editor(SaveManagers.loadMap(file), file)
		}

		mainTable.add(view.mainTable).growY()

		val rightTable = table {
			top()
			image(Drawable(Gdx.files.internal("assets/logo/banner_inv_640.png")), Scaling.fillX).cell( pad = 20f)
			row()
			textButton("Create new world").onChange {
				stage.addActor(window("World creator"){
					addCloseButton()

					label("Name: ")

					val nameField = validatableTextField().cell(padBottom = 2f)

					row()

					val widthModel = IntSpinnerModel(1024, 100, 8192, 1)
					val heightModel = IntSpinnerModel(1024, 100, 8192, 1)

					label("Width: ")

					var widthSpinner = spinner("", widthModel).cell(growX = true, padBottom = 2f)

					row()

					label("Height: ")

					var heightSpinner = spinner("", heightModel).cell(growX = true, padBottom = 2f)

					row()

					val okButton = textButton("OK").cell(growX = true, colspan = 2)
					okButton.onChange {
						fadeOut()
						widthSpinner.validate()
						heightSpinner.validate()
						Main.screen = Editor(ClassicWorld(widthModel.value, heightModel.value, nameField.text))
					}

					var validator = SimpleFormValidator(okButton)
					validator.notEmpty(nameField, "")
					validator.custom(nameField, object: FormInputValidator("File with that name exists") {
						val dir = File("maps/")
						override fun validate(input: String?): Boolean {
							if(input != null) {
								if(dir.resolve(input+".ldmap").exists()) return false
							}
							return true
						}

					})

					pack()
					centerWindow()
				})
			}
		}

		mainTable.add(rightTable).grow()

		stage.addActor(mainTable)
		mainTable.setFillParent(true)
	}

	override fun show() {
		Gdx.input.inputProcessor = stage
		Gdx.graphics.setTitle("LogicDraw ${Version.version}")
	}

	override fun render(delta: Float) {
		Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

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