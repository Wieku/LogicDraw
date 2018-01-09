package me.wieku.circuits.render.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.*
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.util.ToastManager
import com.kotcrab.vis.ui.util.dialog.Dialogs
import com.kotcrab.vis.ui.util.dialog.InputDialogListener
import com.kotcrab.vis.ui.util.dialog.OptionDialogAdapter
import com.kotcrab.vis.ui.util.form.FormInputValidator
import com.kotcrab.vis.ui.util.form.SimpleFormValidator
import com.kotcrab.vis.ui.widget.*
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel
import com.kotcrab.vis.ui.widget.spinner.Spinner
import com.kotcrab.vis.ui.widget.toast.MessageToast
import ktx.vis.*
import me.wieku.circuits.Main
import me.wieku.circuits.api.element.IElement
import me.wieku.circuits.api.element.edit.Editable
import me.wieku.circuits.api.math.Rectangle
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.world.clock.AsyncClock
import me.wieku.circuits.api.world.clock.Updatable
import me.wieku.circuits.input.MapManipulator
import me.wieku.circuits.utils.Palette
import me.wieku.circuits.render.scene.*
import me.wieku.circuits.render.scene.actors.TextTooltip
import me.wieku.circuits.render.scene.editors.HexEditor
import me.wieku.circuits.render.utils.About
import me.wieku.circuits.save.SaveManagers
import me.wieku.circuits.utils.Bresenham
import me.wieku.circuits.utils.Version
import me.wieku.circuits.utils.asString
import me.wieku.circuits.world.ClassicWorld
import me.wieku.circuits.world.ElementRegistry
import me.wieku.circuits.world.WorldClipboard
import org.eclipse.egit.github.core.Gist
import org.eclipse.egit.github.core.GistFile
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.GistService
import java.io.File
import java.io.IOException
import java.lang.reflect.Field
import java.util.*

class Editor(val world: ClassicWorld) : Screen, Updatable.ByTick {

	private lateinit var mainClock: AsyncClock
	private var tickrate = 0L

	private var manipulator: MapManipulator

	private var renderer = ShapeRenderer()
	private var camera: OrthographicCamera
	var stage = Stage(ScreenViewport())
	var toastManager: ToastManager
	private var menuButton: Table

	private var elementTable: Table
	private var tableShow: Boolean = false

	var palette = Palette()

	var tooltip: TextTooltip

	private val gray = Color(0x1f1f1faf)

	private var lastSave = "Not saved"

	private var file: File = File("maps/${world.name.toLowerCase().replace(" ", "_")}.ldmap")

	private lateinit var menuBar: MenuBar

	constructor(world: ClassicWorld, file: File) : this(world) {
		this.file = file
	}

	init {
		camera = object : OrthographicCamera(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()) {
			override fun unproject(screenCoords: Vector3?): Vector3 {
				return super.unproject(screenCoords, 0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height - menuBar.table.height)
			}
		}

		val toastTable = Table()
		toastManager = ToastManager(toastTable)
		toastManager.alignment = Align.bottomLeft

		manipulator = MapManipulator(world, camera, this)

		tooltip = TextTooltip(Color.WHITE)

		camera.zoom = if (stage.width > stage.height) (world.height.toFloat() / stage.height) else (world.width.toFloat() / stage.width)
		camera.position.set(world.width / 2f, world.height / 2f, 0f)

		menuButton = table {
			background = getTxRegion(gray)
			isTransform = true
			rotation = 90f
			pad(2f)
			textButton("Menu").addListener(object : ClickListener() {
				override fun clicked(event: InputEvent?, x: Float, y: Float) {
					super.clicked(event, x, y)
					tableShow = !tableShow
				}
			})
			pack()
		}

		stage.addActor(menuButton)

		elementTable = VisTable()
		elementTable.background = VisUI.getSkin().get("default", Tooltip.TooltipStyle::class.java).background
		elementTable.top().left().pad(5f)

		var count = 0

		ElementRegistry.classes.forEach {
			val color = Color(it.value.getConstructor(Vector2i::class.java).newInstance(Vector2i()).getIdleColor().shl(8) + 0xff)
			val button = me.wieku.circuits.render.scene.ColorButton(color)
			button.addListener(object : ClickListener() {
				override fun clicked(event: InputEvent?, x: Float, y: Float) {
					super.clicked(event, x, y)
					palette.put(it.key)
				}
			})
			button.addListener(tooltip.getListener(it.key))

			elementTable.add(button).pad(3f).expandX().size(40f)
			++count
			if (count == 4) {
				elementTable.row()
				count = 0
			}

		}
		elementTable.row()

		var controls = Label(
				"Controls:\n" +
						"LMB: Pencil\n" +
						"RMB: Eraser\n" +
						"Middle: Pick brush\n" +
						"1..9: Pick from palette\n" +
						"Ctrl+RMB: Edit element\n" +
						"Scroll: Zoom\n" +
						"Shift+LMB: Move canvas\n" +
						"A+LMB: Draw line\n" +
						"D+LMB: Axis aligned line\n" +
						"Ctrl+LMB: Make selection\n" +
						"On selected area:\n" +
						"DEL: Clear selection\n" +
						"F: Fill selection\n" +
						"Ctrl+X: Cut\n" +
						"Ctrl+C: Copy\n" +
						"Ctrl+V: Paste\n" +
						"Ctrl+S: Save world", Color.WHITE, 9)

		elementTable.add(controls).fillX().expandY().colspan(4).bottom()

		stage.addActor(elementTable)

		Gdx.input.inputProcessor = manipulator

		elementTable.touchable = Touchable.enabled

		elementTable.addListener(object : InputListener() {
			override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
				super.touchDown(event, x, y, pointer, button)
				return true
			}

			override fun mouseMoved(event: InputEvent?, x: Float, y: Float): Boolean {
				super.mouseMoved(event, x, y)
				return true
			}
		})
		var max = 0
		world.getStateManager().children.forEach {
			if (it != null) max = Math.max(max, it.holders)
		}
		println("Biggest node size: $max")

		menuBar = menuBar {
			menu("File") {
				menuItem("Save file", Drawable(Gdx.files.internal("assets/icons/save.png"))).setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.S).addListener(object : ChangeListener() {
					override fun changed(event: ChangeEvent?, actor: Actor?) {
						saveFile()
					}
				})

				menuItem("Export as image").onChange {
					this@Editor.stage.addActor(window("Export image"){
						addCloseButton()
						val sizeModel = IntSpinnerModel(1, 1, 16, 1)

						label("Size: ")

						spinner("", sizeModel).cell(growX = true, padBottom = 2f)

						row()

						val okButton = textButton("OK").cell(growX = true, colspan = 2)
						okButton.onChange {
							val isRunning = mainClock.isRunning()

							if(isRunning)
								mainClock.stop()

							var pixmap = Pixmap(world.width*sizeModel.value, world.height*sizeModel.value, Pixmap.Format.RGBA8888)
							pixmap.setColor(Color.BLACK)
							pixmap.fillRectangle(0, 0, pixmap.width-1, pixmap.height-1)
							for(x in 0 until world.width) {
								for (y in 0 until world.height) {
									val el = world[x, y]
									if (el != null) {
										pixmap.setColor(el.getColor().shl(8)+255)
										pixmap.fillRectangle(x*sizeModel.value, y*sizeModel.value, sizeModel.value, sizeModel.value)
									}
								}
							}
							val path = "exports/${world.name.toLowerCase()}_${sizeModel.value}.png"
							try {
								val writer = PixmapIO.PNG((pixmap.width.toFloat() * pixmap.height.toFloat() * 1.5f).toInt()) // Guess at deflated size.
								try {
									writer.setFlipY(false)
									writer.write(Gdx.files.local(path), pixmap)
									toastManager.show(MessageToast("$path exported successfully!"), 5f)
								} finally {
									writer.dispose()
								}
							} catch (ex: IOException) {
								ex.printStackTrace()
								toastManager.show(MessageToast("$path export failed!"), 5f)
							}

							if(isRunning)
								mainClock.start()

							fadeOut()
						}

						pack()
						centerWindow()
					}.fadeIn())
				}

				menuItem("Close project").addListener(object : ChangeListener() {
					override fun changed(event: ChangeEvent?, actor: Actor?) {
						Main.screen = WorldCreator()
					}
				})
			}

			menu("Edit") {

				MenuManager.addDependent("selection", menuItem("Cut").onChange { manipulator.makeCut() })
				MenuManager.addDependent("selection", menuItem("Copy").onChange { manipulator.makeCopy() })
				MenuManager.addDependent("clipboard", menuItem("Paste").onChange { manipulator.makePaste() })

				MenuManager.addDependent("clipboard", menuItem("Transform") {
					subMenu {
						menuItem("Rotate right").onChange { manipulator.clipboard = manipulator.clipboard!!.rotateRight() }
						menuItem("Rotate left").onChange { manipulator.clipboard = manipulator.clipboard!!.rotateLeft() }
						menuItem("Flip horizontal").onChange { manipulator.clipboard = manipulator.clipboard!!.flipHorizontal() }
						menuItem("Flip vertical").onChange { manipulator.clipboard = manipulator.clipboard!!.flipVertical() }
					}
				})

				addSeparator()

				menuItem("Import blueprint...").onChange {
					Dialogs.showInputDialog(this@Editor.stage, "Import", "Gist id", true, object: InputDialogListener {
						override fun canceled() {}

						override fun finished(input: String?) {
							downloadBlueprint(input!!)
						}
					})
				}

				var window: VisWindow? = null
				MenuManager.addDependent("clipboard", menuItem("Save blueprint").onChange {
					if (window == null || !this@Editor.stage.actors.contains(window)) {
						window = window("Save blueprint") {
							addCloseButton()
							center()

							var textField = validatableTextField {name = "Blueprint name"}

							val okButton = textButton("OK").cell(growX = true)
							okButton.onClickS {
								var file = File("blueprints/"+textField.text+".ldbp")
								if(file.exists()) {
									Dialogs.showOptionDialog(this@Editor.stage, "Blueprint exists", "Blueprint with that name exists.\n Do you want to overwrite?", Dialogs.OptionDialogType.YES_CANCEL, object: OptionDialogAdapter() {
										@Override
										override fun yes () {
											saveBlueprint(file)
											fadeOut()
										}

										@Override
										override fun cancel () {}
									})
								} else {
									saveBlueprint(file)
									fadeOut()
								}
							}

							var validator = SimpleFormValidator(okButton)
							validator.notEmpty(textField, "")

							pack()
							centerWindow()
						}
						this@Editor.stage.addActor(window!!.fadeIn())
					}
				})

				var menu = popupMenu {  }
				var load: MenuItem = menuItem("Blueprints") {
					subMenu = menu
				}
				load.addListener(object: InputListener() {
					override fun enter(event: InputEvent?, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
						super.enter(event, x, y, pointer, fromActor)
						if(load.subMenu == menu) {
							load.subMenu = createBPMenu()

							load.listeners.filter { it is InputListener }.forEach { (it as InputListener).enter(event, x, y, pointer, fromActor) }
						}
					}

					override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?) {
						load.subMenu = menu
					}
				})

			}

			menu("Help") {
				menuItem("About").onChange { About.showAboutWindow(this@Editor.stage) }
			}

			table.add(table(true) {
				left()
				val startButton = MenuManager.addDependent("clockRunning", imageButton(Drawable(Gdx.files.internal("assets/icons/play.png"))), true)
				startButton.style.imageDisabled = Drawable(Gdx.files.internal("assets/icons/play_gray.png"))
				startButton.addTextTooltip("Start the clock")
				startButton.isDisabled = true

				val stopButton = MenuManager.addDependent("clockRunning", imageButton(Drawable(Gdx.files.internal("assets/icons/stop.png"))))
				stopButton.style.imageDisabled = Drawable(Gdx.files.internal("assets/icons/stop_gray.png"))
				stopButton.addTextTooltip("Stop the clock")
				stopButton.isDisabled = false

				val stepButton = MenuManager.addDependent("clockRunning", imageButton(Drawable(Gdx.files.internal("assets/icons/forward.png"))), true)
				stepButton.style.imageDisabled = Drawable(Gdx.files.internal("assets/icons/forward_gray.png"))
				stepButton.addTextTooltip("Generate single tick")
				stepButton.isDisabled = true

				val configButton = imageButton(Drawable(Gdx.files.internal("assets/icons/gear.png")))
				configButton.addTextTooltip("Simulation settings")

				startButton.onClickS {
					mainClock.start()
				}

				stopButton.onClickS {
					mainClock.stop()
				}

				stepButton.onClickS {
					mainClock.step()
				}

				var window: VisWindow? = null
				configButton.onClickS {
					if (window == null || !stage.actors.contains(window)) {
						window = window("Simulation settings") {
							addCloseButton()
							val model = IntSpinnerModel(mainClock.tickRate, 1, 1000000, 10)
							val spinner = spinner("Tickrate:", model).cell(growX = true, pad = 2f)
							row()

							val okButton = textButton("OK").cell(growX = true)
							okButton.onClickS {
								val isRunning = mainClock.isRunning()

								if(isRunning) mainClock.stop()

								mainClock.tickRate = model.value

								if(isRunning) mainClock.start()
								fadeOut()
							}

							val listener = object : ChangeListener() {
								override fun changed(event: ChangeEvent?, actor: Actor?) {
									okButton.isDisabled = !spinner.textField.isInputValid
								}
							}

							spinner.textField.addListener(listener)
							spinner.addListener(listener)

							pack()

							val vector = configButton.localToStageCoordinates(Vector2(0f, 0f))
							setPosition(vector.x, vector.y - height)
						}
						stage.addActor(window!!.fadeIn())
					}
				}
				pack()
			}).right().expandX()

		}

		stage.addActor(menuBar.table)

		toastTable.setFillParent(true)
		toastTable.addActor(tooltip.tooltipTable)
		stage.addActor(toastTable)
	}

	private fun createBPMenu() : PopupMenu {
		return popupMenu {
			val dir = File("blueprints/")
			dir.mkdirs()
			dir.listFiles().filter { it.extension == "ldbp" }.forEach {
				menuItem(it.nameWithoutExtension).subMenu {

					menuItem("Convert").onChange {
						try {
							val blueprint = SaveManagers.loadBlueprint(it)
							SaveManagers.saveBlueprint(blueprint, file)
							toastManager.show(MessageToast("Blueprint converted!"), 5f)
						} catch (e: Exception) {
							toastManager.show(MessageToast("Error converting blueprint!"), 5f)
						}
					}

					menuItem("Load").onChange { loadBlueprint(it) }

					menuItem("Upload to gist").subMenu {
						menuItem("Public").onChange {
							uploadBlueprint(it, true)
						}

						menuItem("Private").onChange {
							uploadBlueprint(it, false)
						}
					}
				}
			}
		}
	}

	fun uploadBlueprint(file: File, public: Boolean) {
		try {
			val client = GitHubClient()
			var gist = Gist().setPublic(public).setDescription("")
			val gistFile = GistFile().setContent(Base64.getEncoder().encodeToString(file.readBytes()))
			gist.files = Collections.singletonMap(file.name+".b64", gistFile)
			gist = GistService(client).createGist(gist)
			val toast = MessageToast("Blueprint uploaded!")
			Gdx.app.clipboard.contents = gist.htmlUrl.toString()
			toast.addLinkLabel(gist.htmlUrl.toString(), null)
			toastManager.show(toast, 5f)
		} catch (e: Exception) {
			e.printStackTrace()
			toastManager.show(MessageToast("Blueprint upload failed!"), 5f)
		}
	}

	fun downloadBlueprint(id: String) {
		try {
			val service = GistService()
			var gist = service.getGist(id.split("/").last())
			for(file in gist.files) {
				if(file.key.endsWith(".ldbp.b64")) {
					var binFile = File("blueprints/"+file.key.replace(".b64", ""))
					if(!binFile.exists()) {
						binFile.writeBytes(Base64.getDecoder().decode(file.value.content))
						toastManager.show(MessageToast("Blueprint ${binFile.name} imported!"), 5f)
					} else {
						Dialogs.showOptionDialog(this@Editor.stage, "Blueprint exists", "Blueprint with that name exists.\n Do you want to overwrite?", Dialogs.OptionDialogType.YES_NO_CANCEL, object: OptionDialogAdapter() {
							@Override
							override fun yes () {
								binFile.writeBytes(Base64.getDecoder().decode(file.value.content))
								toastManager.show(MessageToast("Blueprint ${binFile.name} imported!"), 5f)
							}

							@Override
							override fun no () {
								stage.addActor(window("Blueprint exists") {
									addCloseButton()
									center()
									label("Please put alternative name")
									row()
									val textField = validatableTextField {name = "Blueprint name"}
									val okButton = textButton("OK").cell(growX = true)
									okButton.onChange {
										binFile = File("blueprints/"+textField.text+".ldbp")
										binFile.writeBytes(Base64.getDecoder().decode(file.value.content))
										toastManager.show(MessageToast("Blueprint ${binFile.name} imported!"), 5f)
										fadeOut()
									}

									var validator = SimpleFormValidator(okButton)
									validator.notEmpty(textField, "")
									validator.custom(textField, object: FormInputValidator("File with that name exists") {
										val dir = File("blueprints/")
										override fun validate(input: String?): Boolean {
											if(input != null) {
												dir.listFiles().forEach {
													if(it.name == input + ".ldbp") return false }
											}
											return true
										}

									})
									pack()
									centerWindow()
								}.fadeIn())
							}

						})
					}
				}
			}
		} catch (e: Exception) {
			e.printStackTrace()
			toastManager.show(MessageToast("Blueprint import failed!"), 5f)
		}
	}

	fun loadBlueprint(file: File) {

		try {
			val world = SaveManagers.loadBlueprint(file)
			manipulator.clipboard = WorldClipboard.create(Rectangle(0, 0, world.width, world.height), world)
			toastManager.show(MessageToast("Blueprint loaded!"), 5f)
		} catch (e: Exception) {
			toastManager.show(MessageToast("Error loading blueprint!"), 5f)
		}

	}

	fun saveBlueprint(file: File) {
		if(manipulator.clipboard != null) {
			try {
				val blueprint = ClassicWorld(manipulator.clipboard!!.width, manipulator.clipboard!!.height, file.nameWithoutExtension)
				blueprint.pasteNT(Vector2i(0, 0), manipulator.clipboard!!)
				SaveManagers.saveBlueprint(blueprint, file)
				toastManager.show(MessageToast("Blueprint saved!"), 5f)
			} catch (e: Exception) {
				toastManager.show(MessageToast("Error saving blueprint!"), 5f)
			}
		}
	}

	fun saveFile() {
		val isRunning = mainClock.isRunning()
		if(isRunning)
			mainClock.stop()

		try {
			SaveManagers.saveMap(world, file)
			lastSave = "Last saved: " + Date().asString()
			toastManager.show(MessageToast("File saved succesfully!"), 5f)
		} catch (err: Exception) {
			lastSave = "Error saving file!!!"
		}

		if(isRunning)
			mainClock.start()
	}


	override fun show() {
		mainClock = AsyncClock(this, 1000)
		world.clock = mainClock
		mainClock.start()
	}


	private var color = Color()
	private val bound = Color(0.2f, 0.2f, 0.2f, 0.6f)
	private val paletteColor = Color(0x212121ef)
	private var delta1 = 0f
	override fun render(delta: Float) {
		Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

		if(!mainClock.isRunning()) {
			world.updateTasks()
		}

		MenuManager.updateDependency("selection", manipulator.rectangle != null)
		MenuManager.updateDependency("clipboard", manipulator.clipboard != null)
		MenuManager.updateDependency("clockRunning", mainClock.isRunning())

		delta1 += delta
		if (delta1 >= 1f) {
			Gdx.graphics.setTitle("LogicDraw ${Version.version} (world: ${world.name}) (tickrate: $tickrate) (fps: ${Gdx.graphics.framesPerSecond}) (${world.getStateManager().usedNodes} nodes) (${world.entities} entities) | $lastSave")
			delta1 = 0f
		}

		elementTable.setBounds(if (tableShow) stage.width - 200f else stage.width, 0f, 200f, stage.height - menuBar.table.height)
		menuButton.setPosition(elementTable.x, stage.height - menuBar.table.height - menuButton.width - 10)

		camera.update()
		renderer.projectionMatrix = camera.combined

		Gdx.gl.glEnable(GL20.GL_BLEND)
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
		Gdx.gl.glViewport(0, 0, Gdx.graphics.width, (Gdx.graphics.height - menuBar.table.height).toInt())

		renderer.begin(ShapeRenderer.ShapeType.Filled)
		renderer.color = Color.BLACK
		renderer.rect(0f, 0f, world.width.toFloat(), world.height.toFloat())
		for (x in 0 until world.width) {
			for (y in 0 until world.height) {
				val el = world[x, y]
				if (el != null) {
					renderer.color = color.set((el.getColor().shl(8)) + 0xFF)
					renderer.rect(x.toFloat(), y.toFloat(), 1f, 1f)
				}
			}
		}

		if (manipulator.pasteMode || manipulator.rectangle != null) {
			if (manipulator.rectangle != null) {
				renderer.color = bound
				renderer.rect(manipulator.rectangle!!.x.toFloat(), manipulator.rectangle!!.y.toFloat(), manipulator.rectangle!!.width.toFloat(), manipulator.rectangle!!.height.toFloat())
			}

			if (manipulator.pasteMode) {
				manipulator.clipboard!!.drawClipboard(manipulator.position, renderer)
			}
		} else {
			if(palette.currentBrush != null) {
				renderer.color = ElementRegistry.brushes[palette.currentBrush!!]
				if(manipulator.lineMode) {
					Bresenham.iterateFast(manipulator.beginPos, manipulator.endPos) {
						renderer.rect(it.x.toFloat(), it.y.toFloat(), 1f, 1f)
					}
				} else if (manipulator.position.isInBounds(0, 0, world.width - 1, world.height - 1)) {
					renderer.rect(manipulator.position.x.toFloat(), manipulator.position.y.toFloat(), 1f, 1f)
				}
			}
		}

		renderer.end()
		Gdx.gl.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)

		stage.act(delta)
		stage.draw()

		Gdx.gl.glEnable(GL20.GL_BLEND)
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
		renderer.projectionMatrix = stage.camera.combined
		renderer.begin(ShapeRenderer.ShapeType.Filled)

		renderer.color = paletteColor

		val baseX = (stage.width-40*palette.palette.size-if(tableShow) 200 else 0)/2f
		renderer.rect(baseX, 10f,40f*palette.palette.size, 40f)

		for(i in 0 until palette.palette.size) {
			if(i==palette.current) {
				renderer.color = bound
				renderer.rect(baseX + i*40f, 10f, 40f, 40f)
			}
			if(palette.palette[i] != null) {
				renderer.color = ElementRegistry.brushes[palette.palette[i]]
				renderer.rect(baseX +5f+i*40f, 15f, 30f, 30f)
			}
		}

		renderer.end()
		Gdx.gl.glDisable(GL20.GL_BLEND)
	}

	override fun resize(width: Int, height: Int) {
		val camPosBefore = camera.position.cpy()

		stage.viewport.update(width, height, true)

		menuBar.table.pack()
		menuBar.table.width = stage.width
		menuBar.table.setPosition(0f, stage.height - menuBar.table.height)

		camera.setToOrtho(true, width.toFloat(), height - menuBar.table.height)
		camera.position.set(camPosBefore)
		camera.fit(world, stage)
		toastManager.resize()
	}

	override fun dispose() {
		mainClock.stop()
		renderer.dispose()
	}


	//TODO: Move this to other class
	var editorWindow: VisWindow? = null
	fun editElement(element: IElement) {
		if(element is Editable) {
			var fields = ElementRegistry.editors[element!!.javaClass]
			if (editorWindow == null || !stage.actors.contains(editorWindow)) {
				editorWindow = window("${ElementRegistry.names[element!!.javaClass]} settings") {
					addCloseButton()

					val spinners = HashMap<Spinner, Field>()
					val radioButtons = HashMap<VisRadioButton, Field>()

					val keyStore = HashMap<VisTextButton, Int>()
					val keySelectors = HashMap<VisTextButton, Field>()

					val hexEditors = HashMap<HexEditor, Field>()

					for(field in fields!!) {
						if(field.annotation is Editable.Spinner) {
							val amodel = field.annotation.model

							val jField = field.field
							jField.isAccessible = true

							val model = IntSpinnerModel(jField.getInt(element), amodel[1], amodel[2], amodel[3])
							val spinner = spinner(field.annotation.name, model).cell(growX = true, pad = 2f)
							spinners.put(spinner, jField)
							row()
						}

						if(field.annotation is Editable.Hex) {
							val hexEditor = HexEditor()
							val jField = field.field
							jField.isAccessible = true
							hexEditor.loadFromBytes(jField.get(element) as ByteArray)
							hexEditors.put(hexEditor, jField)
							add(hexEditor)
							row()
						}

						if(field.annotation is Editable.Boolean) {
							val jField = field.field
							jField.isAccessible = true

							radioButtons.put(radioButton(field.annotation.name) {isChecked = jField.getBoolean(element)}, jField)

							row()
						}

						if(field.annotation is Editable.Key) {
							val jField = field.field
							jField.isAccessible = true

							table {
								label(field.annotation.name+": ")
								val button = textButton(Input.Keys.toString(jField.getInt(element))).cell(width = 100f)

								keyStore.put(button, jField.getInt(element))
								keySelectors.put(button, jField)

								button.onChange {
									if(!button.isDisabled){
										button.isDisabled = true
									}
								}

								this@Editor.stage.addListener(object: InputListener() {
									override fun keyDown(event: InputEvent?, keycode: Int): Boolean {
										if(button.isDisabled) {
											if(keycode != Input.Keys.ESCAPE) {
												keyStore.put(button, keycode)
												button.setText(Input.Keys.toString(keycode))
											}
											button.isDisabled = false
											return true
										}
										return super.keyDown(event, keycode)
									}
								})

							}

							row()
						}

					}

					val okButton = textButton("OK").cell(growX = true)
					okButton.onClickS {
						spinners.forEach { t, u -> u.setInt(element, (t.model as IntSpinnerModel).value) }
						radioButtons.forEach { t, u -> u.setBoolean(element, t.isChecked) }
						keySelectors.forEach { t, u -> u.setInt(element, keyStore[t]!!) }
						hexEditors.forEach { t, u -> u.set(element, t.saveToBytes()) }
						fadeOut()
					}

					val listener = object : ChangeListener() {
						override fun changed(event: ChangeEvent?, actor: Actor?) {
							var check = true
							for(spinner in spinners)
								check = check && spinner.key.textField.isInputValid
							okButton.isDisabled = !check
						}
					}

					spinners.forEach {
						it.key.textField.addListener(listener)
						it.key.addListener(listener)
					}

					pack()

					centerWindow()
				}
				stage.addActor(editorWindow!!.fadeIn())
			}
		}
	}

	private var time = System.nanoTime()
	private var tick = 0L
	override fun update(value: Long) {
		world.update(value)
		if (System.nanoTime() - time >= 1000000000) {
			tickrate = value - tick
			tick = value
			time = System.nanoTime()
		}
	}

	override fun hide() {}

	override fun pause() {
		manipulator.pause = true
	}

	override fun resume() {}
}