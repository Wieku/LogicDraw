package me.wieku.circuits.render.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.util.adapter.ArrayListAdapter
import com.kotcrab.vis.ui.util.form.FormInputValidator
import com.kotcrab.vis.ui.util.form.SimpleFormValidator
import com.kotcrab.vis.ui.widget.ListView
import com.kotcrab.vis.ui.widget.VisTable
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel
import ktx.vis.table
import ktx.vis.window
import me.wieku.circuits.Main
import me.wieku.circuits.render.scene.Drawable
import me.wieku.circuits.render.scene.getTxRegion
import me.wieku.circuits.render.scene.onChange
import me.wieku.circuits.save.SaveManagers
import me.wieku.circuits.utils.Version
import me.wieku.circuits.world.ClassicWorld
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class WorldCreator:Screen {

	private var stage: Stage = Stage(ScreenViewport())

	private var mainTable = Table()

	data class LDMap(val file: File, val description: Array<String>)

	init {
		File("maps/").mkdir()
		File("blueprints/").mkdir()
		mainTable.top().left()

		val maps: ArrayList<LDMap>? = ArrayList()/*File("maps/").listFiles().filter{!it.isDirectory && it.extension == "ldmap"}.mapNotNull {
			val array = SaveManagers.getHeader(it)
			if(array != null) {
				LDMap(it, array)
			} else null
		}.toMutableList() as ArrayList<LDMap>*/

		val adapter = object: ArrayListAdapter<LDMap, VisTable>(maps) {
			private val bg = VisUI.getSkin().getDrawable("window-bg")
			private val selection = VisUI.getSkin().getDrawable("list-selection")

			init {
				selectionMode = SelectionMode.SINGLE
			}

			override fun createView(item: LDMap): VisTable {
				return table {
					background = bg

					image(getTxRegion(Color.WHITE), Scaling.stretchX).cell(fillX = true)
					row()
					table {
						left()
						label(item.description[0]).cell(padBottom = 3f, align = Align.top)
						row()
						label("Size: ${item.description[1]}x${item.description[2]}").cell(align = Align.left, growX = true)
						row()
						label("File: ${item.file.name}").cell(align = Align.left, growX = true)
						pad(5f)
					}.cell(growX = true)
					row()
					image(getTxRegion(Color.WHITE), Scaling.stretchX).cell(fillX = true)
				}
			}

			override fun selectView(view: VisTable?) {
				view!!.setBackground(selection)
			}

			override fun deselectView(view: VisTable?) {
				view!!.setBackground(bg)
			}

		}

		val view = ListView<LDMap>(adapter)

		view.scrollPane.setScrollingDisabled(true, false)

		view.mainTable.background = getTxRegion(Color(0.08f, 0.08f, 0.08f, 1f))

		view.setItemClickListener {
			var file = it.file
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
	}

	override fun dispose() {
		stage.dispose()
	}

	override fun pause() {}

	override fun resume() {}

	override fun hide() {}
}