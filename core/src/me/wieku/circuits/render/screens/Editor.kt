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
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.util.ToastManager
import com.kotcrab.vis.ui.util.dialog.Dialogs
import com.kotcrab.vis.ui.util.dialog.InputDialogListener
import com.kotcrab.vis.ui.util.dialog.OptionDialogAdapter
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
import me.wieku.circuits.render.scene.*
import me.wieku.circuits.render.scene.actors.TextTooltip
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

	private var brushes: HashMap<String, Color> = HashMap()

	var tooltip: TextTooltip

	private val gray = Color(0x1f1f1faf)

	private var lastSave = "Not saved"

	private var file: File = File("maps/${world.name.toLowerCase().replace(" ", "_")}.ldmap")

	private var menuBar: MenuBar
	private lateinit var simulationBar: VisTable

	private lateinit var startButton:VisImageButton
	private lateinit var stopButton:VisImageButton
	private lateinit var stepButton:VisImageButton

	constructor(world: ClassicWorld, file: File) : this(world) {
		this.file = file
	}

	init {
		camera = object : OrthographicCamera(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()) {
			override fun unproject(screenCoords: Vector3?): Vector3 {
				return super.unproject(screenCoords, 0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height - simulationBar.height)
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
			var color = Color(it.value.getConstructor(Vector2i::class.java).newInstance(Vector2i()).getIdleColor().shl(8) + 0xff)
			var color1 = color.cpy()
			color1.a = 0.5f
			brushes.put(it.key, color1)
			var button = me.wieku.circuits.render.scene.ColorButton(color)
			button.addListener(object : ClickListener() {
				override fun clicked(event: InputEvent?, x: Float, y: Float) {
					super.clicked(event, x, y)
					manipulator.toPlace = it.key
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

		//NOTE: THIS WILL BE ADDED WITH UI DESIGN UPDATE
		/*var imageButton = TextButton("Export as image", getTextButtonStyle(Color.BLACK, Color.WHITE, 13))
		imageButton.addListener(object: ClickListener(){
			override fun clicked(event: InputEvent?, x: Float, y: Float) {
				mainClock.stop()
				var pixmap = Pixmap(world.width*8, world.height*8, Pixmap.Format.RGBA8888)
				pixmap.setColor(Color.BLACK)
				pixmap.fillRectangle(0, 0, pixmap.width-1, pixmap.height-1)
				for(x in 0 until world.width) {
					for (y in 0 until world.height) {
						val el = world[x, y]
						if (el != null) {
							pixmap.setColor(el.getColor().shl(8)+255)
							pixmap.fillRectangle(x*8, y*8, 8, 8)
						}
					}
				}
				PixmapIO.writePNG(Gdx.files.local(file.name.split(".")[0]+".png"), pixmap)
				mainClock.start()
			}
		})

		elementTable.add(imageButton).fillX().center().padTop(10f).colspan(4).row()*/

		var controls = Label(
				"Controls:\n" +
						"LMB: Pencil\n" +
						"RMB: Eraser\n" +
						"Middle: Pick brush\n" +
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
							for(listener in load.listeners) {
								if(listener is InputListener) run {
									listener.enter(event, x, y, pointer, fromActor)
								}
							}
						}
					}

					override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?) {
						load.subMenu = menu
					}
				})

			}

			menu("Help") {

				var window: VisWindow? = null
				menuItem("About").addListener(object : ChangeListener() {
					override fun changed(event: ChangeEvent?, actor: Actor?) {
						if (window == null || !this@Editor.stage.actors.contains(window)) {
							window = window("About") {
								addCloseButton()
								center()
								image(Drawable(Gdx.files.internal("assets/logo/banner_inv_320.png")), Scaling.fillX)

								row()
								label("Version: ${Version.version}")

								row()
								linkLabel("GitHub", "https://github.com/Wieku/LogicDraw")

								row()
								label("Powered by open-source")

								row()

								val okButton = textButton("OK").cell(growX = true)
								okButton.onClickS {
									fadeOut()
								}
								pack()
								centerWindow()
							}
							this@Editor.stage.addActor(window!!.fadeIn())
						}
					}
				})
			}
		}

		stage.addActor(menuBar.table)

		simulationBar = table(true) {
			//background = VisUI.getSkin().get("default", Tooltip.TooltipStyle::class.java).background
			left()
			startButton = imageButton(Drawable(Gdx.files.internal("assets/icons/play.png")))
			startButton.style.imageDisabled = Drawable(Gdx.files.internal("assets/icons/play_gray.png"))
			startButton.addTextTooltip("Start the clock")
			startButton.isDisabled = true

			stopButton = imageButton(Drawable(Gdx.files.internal("assets/icons/stop.png")))
			stopButton.style.imageDisabled = Drawable(Gdx.files.internal("assets/icons/stop_gray.png"))
			stopButton.addTextTooltip("Stop the clock")
			stopButton.isDisabled = false

			stepButton = imageButton(Drawable(Gdx.files.internal("assets/icons/forward.png")))
			stepButton.style.imageDisabled = Drawable(Gdx.files.internal("assets/icons/forward_gray.png"))
			stepButton.addTextTooltip("Generate single tick")
			stepButton.isDisabled = true

			val configButton = imageButton(Drawable(Gdx.files.internal("assets/icons/gear.png")))
			configButton.addTextTooltip("Simulation settings")

			startButton.onClickS {
				mainClock.start()
				startButton.isDisabled = true
				stepButton.isDisabled = true
				stopButton.isDisabled = false
			}

			stopButton.onClickS {
				mainClock.stop()
				startButton.isDisabled = false
				stepButton.isDisabled = false
				stopButton.isDisabled = true
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

						if(isRunning)
							mainClock.stop()
						mainClock.tickRate = model.value
						if(isRunning)
							mainClock.start()
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
	}

		menuBar.table.add(simulationBar).right().expandX()

		toastTable.setFillParent(true)
		toastTable.addActor(tooltip.tooltipTable)
		stage.addActor(toastTable)
	}

	private fun createBPMenu() : PopupMenu {
		return popupMenu {

			//menuItem("Import blueprint (IPFS").isDisabled = true
			val dir = File("blueprints/")
			dir.mkdirs()
			dir.listFiles().filter { it.extension == "ldbp" }.forEach {
				menuItem(it.nameWithoutExtension).subMenu {

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

	fun downloadBlueprint(gist: String) {
		try {
			val client = GitHubClient()
			val service = GistService()
			var gist = service.getGist(gist)
			for(file in gist.files) {
				if(file.key.endsWith(".ldbp.b64")) {
					var binFile = File("blueprints/"+file.key.replace(".b64", ""))
					if(!binFile.exists()) {
						binFile.writeBytes(Base64.getDecoder().decode(file.value.content))
						toastManager.show(MessageToast("Blueprint ${binFile.name} imported!"), 5f)
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
				blueprint.paste(Vector2i(0, 0), manipulator.clipboard!!)
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
	private var delta1 = 0f
	override fun render(delta: Float) {
		Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

		MenuManager.updateDependency("selection", manipulator.rectangle != null)
		MenuManager.updateDependency("clipboard", manipulator.clipboard != null)

		delta1 += delta
		if (delta1 >= 1f) {
			if(!mainClock.isRunning()) {
				startButton.isDisabled = false
				stepButton.isDisabled = false
				stopButton.isDisabled = true
			}
			Gdx.graphics.setTitle("LogicDraw ${Version.version} (world: ${world.name}) (tickrate: $tickrate) (fps: ${Gdx.graphics.framesPerSecond}) (${world.getStateManager().usedNodes} nodes) (${world.entities} entities) | $lastSave")
			delta1 = 0f
		}

		elementTable.setBounds(if (tableShow) stage.width - 200f else stage.width, 0f, 200f, stage.height /*- simulationBar.height*/ - menuBar.table.height)
		menuButton.setPosition(elementTable.x, stage.height /*- simulationBar.height*/ - menuBar.table.height - menuButton.width - 10)
		menuButton.color = if (tableShow) Color.WHITE else Color.BLACK

		camera.update()
		renderer.projectionMatrix = camera.combined

		Gdx.gl.glEnable(GL20.GL_BLEND)
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
		Gdx.gl.glViewport(0, 0, Gdx.graphics.width, (Gdx.graphics.height - simulationBar.height).toInt())
		renderer.begin(ShapeRenderer.ShapeType.Filled)
		renderer.color = Color.BLACK
		renderer.rect(0f, 0f, world.width.toFloat(), world.height.toFloat())
		for (x in 0 until world.width) {
			for (y in 0 until world.height) {
				val el = world[x, y]
				if (el != null) {
					color.set((el.getColor().shl(8)) + 0xFF)
					renderer.color = color
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
			if (manipulator.lineMode) {
				renderer.color = brushes[manipulator.toPlace]
				Bresenham.iterateFast(manipulator.beginPos, manipulator.endPos) {
					renderer.rect(it.x.toFloat(), it.y.toFloat(), 1f, 1f)
				}
			}
			if (manipulator.position.isInBounds(0, 0, world.width - 1, world.height - 1)) {
				renderer.color = brushes[manipulator.toPlace]
				renderer.rect(manipulator.position.x.toFloat(), manipulator.position.y.toFloat(), 1f, 1f)
			}
		}

		renderer.end()
		Gdx.gl.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
		Gdx.gl.glDisable(GL20.GL_BLEND)
		stage.act(delta)
		stage.draw()
	}

	override fun resize(width: Int, height: Int) {
		val camPosBefore = camera.position.cpy()

		stage.viewport.update(width, height, true)

		menuBar.table.pack()
		menuBar.table.width = width.toFloat()
		menuBar.table.setPosition(0f, stage.height - menuBar.table.height)

		//simulationBar.width = width.toFloat()
		//simulationBar.setPosition(0f, stage.height - simulationBar.height - menuBar.table.height)

		camera.setToOrtho(true, width.toFloat(), height /*- simulationBar.height*/ - menuBar.table.height)
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

					for(field in fields!!) {
						if(field.annotation is Editable.Spinner) {
							val amodel = field.annotation.model

							val jField = element.javaClass.getDeclaredField(field.name)
							jField.isAccessible = true

							val model = IntSpinnerModel(jField.getInt(element), amodel[1], amodel[2], amodel[3])
							val spinner = spinner(field.annotation.name, model).cell(growX = true, pad = 2f)
							spinners.put(spinner, jField)
							row()
						}
					}

					val okButton = textButton("OK").cell(growX = true)
					okButton.onClickS {
						spinners.forEach { t, u -> u.setInt(element, (t.model as IntSpinnerModel).value) }
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

	override fun pause() {}

	override fun resume() {}
}