package me.wieku.circuits.render.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.*
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import me.wieku.circuits.Main
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.world.clock.AsyncClock
import me.wieku.circuits.api.world.clock.Updatable
import me.wieku.circuits.input.MapManipulator
import me.wieku.circuits.render.scene.Label
import me.wieku.circuits.render.scene.actors.TextTooltip
import me.wieku.circuits.render.scene.fit
import me.wieku.circuits.render.scene.getTextButtonStyle
import me.wieku.circuits.save.SaveManagers
import me.wieku.circuits.utils.Bresenham
import me.wieku.circuits.utils.Version
import me.wieku.circuits.utils.asString
import me.wieku.circuits.world.ClassicWorld
import java.io.File
import java.util.*

class Editor(val world: ClassicWorld):Screen, Updatable.ByTick {

	private lateinit var mainClock:AsyncClock
	private var tickrate = 0L

	private var manipulator: MapManipulator

	private var renderer: ShapeRenderer
	private var camera: OrthographicCamera
	private var stage: Stage
	private var menuButton: Table
	private var saveButton: TextButton

	private var elementTable: Table
	private var tableShow: Boolean = false

	private var brushes: HashMap<String, Color> = HashMap()

	var tooltyp: TextTooltip

	private val gray = Color(0x1f1f1faf)

	private var lastSave = "Not saved"

	init {
		renderer = ShapeRenderer()
		camera = OrthographicCamera(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
		stage = Stage(ScreenViewport())
		manipulator = MapManipulator(world, camera, stage)

		tooltyp = TextTooltip(Color.BLACK, Color.WHITE, 10)

		camera.zoom = if(stage.width > stage.height) (world.height.toFloat()/stage.height) else (world.width.toFloat()/stage.width)
		camera.position.set(world.width/2f, world.height/2f, 0f)
		menuButton = me.wieku.circuits.render.scene.Table(gray)
		menuButton.isTransform = true
		var buttn = TextButton("Menu", getTextButtonStyle(Color.WHITE, 15))
		buttn.addListener(object: ClickListener(){
			override fun clicked(event: InputEvent?, x: Float, y: Float) {
				super.clicked(event, x, y)
				tableShow = !tableShow
			}
		})
		menuButton.add(buttn).pad(2f)

		menuButton.rotation = 90f
		menuButton.pack()
		stage.addActor(menuButton)
		elementTable = me.wieku.circuits.render.scene.Table(gray)
		elementTable.top().left().pad(5f)

		var count = 0
		world.classes.forEach{
			var color = Color(it.value.getConstructor(Vector2i::class.java).newInstance(Vector2i()).getIdleColor().shl(8)+0xff)
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
			button.addListener(tooltyp.getListener(it.key))

			elementTable.add(button).pad(3f).expandX().size(40f)
			++count
			if(count==4) {
				elementTable.row()
				count = 0
			}

		}
		elementTable.row()

		saveButton = TextButton("Save", getTextButtonStyle(Color.BLACK, Color.WHITE, 15))
		saveButton.addListener(object: ClickListener(){
			override fun clicked(event: InputEvent?, x: Float, y: Float) {
				mainClock.stop()
				try {
					SaveManagers.saveMap(world, File("maps/${world.name.toLowerCase().replace(" ", "_")}.ldmap"))
					lastSave = "Last saved: " + Date().asString()
				} catch (err: Exception) {
					lastSave = "Error saving file!!!"
				}

				mainClock.start()
			}
		})

		elementTable.add(saveButton).fillX().center().padTop(10f).colspan(4).row()

		var exitButton = TextButton("Exit", getTextButtonStyle(Color.BLACK, Color.WHITE, 15))
		exitButton.addListener(object: ClickListener(){
			override fun clicked(event: InputEvent?, x: Float, y: Float) {
				Main.screen = WorldCreator()
			}
		})

		elementTable.add(exitButton).fillX().center().padTop(10f).colspan(4).row()

		var clockButton = TextButton("Stop clock", getTextButtonStyle(Color.BLACK, Color.WHITE, 15))

		var stepButton = TextButton("Make step", getTextButtonStyle(Color.BLACK, Color.WHITE, 15))

		stepButton.isDisabled = true

		clockButton.addListener(object: ClickListener(){
			override fun clicked(event: InputEvent?, x: Float, y: Float) {
				if(clockButton.text.trim() == "Stop clock") {
					println("ffeergrdgrd")
					mainClock.stop()
					clockButton.setText("Start clock")
					stepButton.isDisabled = false
				} else if(clockButton.text.trim() == "Start clock") {
					mainClock.start()
					clockButton.setText("Stop clock")
					stepButton.isDisabled = true
				}

			}
		})



		elementTable.add(clockButton).fillX().center().padTop(40f).colspan(4).row()

		stepButton.addListener(object: ClickListener(){
			override fun clicked(event: InputEvent?, x: Float, y: Float) {
				if(!stepButton.isDisabled)
					mainClock.step()
			}
		})

		elementTable.add(stepButton).fillX().center().padTop(10f).colspan(4).row()


		var controls = Label(
				"Controls:\n" +
						"LMB: Pencil\n" +
						"RMB: Eraser\n" +
						"Middle: Pick brush\n" +
						"Scroll: Zoom\n" +
						"Shift+LMB: Move canvas\n" +
						"A+LMB: Draw line\n" +
						"D+LMB: Axis aligned line\n" +
						"Ctrl+LMB: Make selection\n" +
						"On selected area:\n" +
						"DEL: Clear selection\n" +
						"S: Fill selection\n" +
						"Ctrl+X: Cut\n" +
						"Ctrl+C: Copy\n" +
						"Ctrl+V: Paste", Color.WHITE, 9)

		elementTable.add(controls).fillX().expandY().colspan(4).bottom()

		stage.addActor(elementTable)

		stage.addActor(tooltyp.tooltipTable)

		Gdx.input.inputProcessor = manipulator

		elementTable.touchable = Touchable.enabled

		elementTable.addListener(object: InputListener(){
			override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
				super.touchDown(event, x, y, pointer, button)
				return true
			}
		})

	}

	override fun show() {
		mainClock = AsyncClock(this, 1000)
		mainClock.start()
	}


	private var color = Color()
	private val bound = Color(0.2f, 0.2f, 0.2f, 0.6f)
	private var delta1 = 0f
	override fun render(delta: Float) {
		Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

		delta1+=delta
		if(delta1>=1f) {
			Gdx.graphics.setTitle("LogicDraw ${Version.version} (world: ${world.name}) (tickrate: $tickrate) (fps: ${Gdx.graphics.framesPerSecond}) (${world.getStateManager().usedNodes} nodes) (${world.entities} entities) | $lastSave")
			delta1 = 0f
		}

		elementTable.setBounds(if(tableShow) stage.width-200f else stage.width, 0f, 200f, stage.height)
		menuButton.setPosition(elementTable.x, stage.height-menuButton.width-10)
		menuButton.color = if(tableShow) Color.WHITE else Color.BLACK

		camera.update()
		renderer.projectionMatrix = camera.combined

		Gdx.gl.glEnable(GL20.GL_BLEND)
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
		renderer.begin(ShapeRenderer.ShapeType.Filled)
		renderer.color = Color.BLACK
		renderer.rect(0f, 0f, world.width.toFloat(), world.height.toFloat())
		for(x in 0 until world.width) {
			for(y in 0 until world.height) {
				val el = world[x, y]
				if(el != null) {
					color.set((el.getColor().shl(8)) + 0xFF)
					renderer.color = color
					renderer.rect(x.toFloat(), y.toFloat(), 1f, 1f)
				}
			}
		}
		if(manipulator.pasteMode || manipulator.rectangle != null) {
			if(manipulator.rectangle != null) {
				renderer.color = bound
				renderer.rect(manipulator.rectangle!!.x.toFloat(), manipulator.rectangle!!.y.toFloat(), manipulator.rectangle!!.width.toFloat(), manipulator.rectangle!!.height.toFloat())
			}

			if(manipulator.pasteMode) {
				manipulator.clipboard!!.drawClipboard(manipulator.position, renderer)
			}
		} else {
			if(manipulator.lineMode) {
				renderer.color = brushes[manipulator.toPlace]
				Bresenham.iterateFast(manipulator.beginPos, manipulator.endPos) {
					renderer.rect(it.x.toFloat(), it.y.toFloat(), 1f, 1f)
				}
			}
			if(manipulator.position.isInBounds(0, 0, world.width-1, world.height-1)) {
				renderer.color = brushes[manipulator.toPlace]
				renderer.rect(manipulator.position.x.toFloat(), manipulator.position.y.toFloat(), 1f, 1f)
			}
		}

		renderer.end()
		Gdx.gl.glDisable(GL20.GL_BLEND)
		stage.act()
		stage.draw()
	}

	override fun resize(width: Int, height: Int) {
		val camPosBefore = camera.position.cpy()

		stage.viewport.update(width, height, true)

		camera.setToOrtho(true, width.toFloat(), height.toFloat())
		camera.position.set(camPosBefore)
		camera.fit(world, stage)
	}

	override fun dispose() {
		mainClock.stop()
		renderer.dispose()
	}

	private var time = System.nanoTime()
	private var tick = 0L
	override fun update(value: Long) {
		world.update(value)
		if(System.nanoTime()-time >= 1000000000) {
			tickrate = value-tick
			tick = value
			time = System.nanoTime()
		}
	}

	override fun hide() {}

	override fun pause() {}

	override fun resume() {}
}