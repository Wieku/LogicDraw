package me.wieku.circuits

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.world.clock.AsyncClock
import me.wieku.circuits.api.world.clock.Updatable
import me.wieku.circuits.input.MapManipulator
import me.wieku.circuits.render.utils.*
import me.wieku.circuits.world.ClassicWorld
import java.util.*

class Main : ApplicationAdapter(), Updatable.ByTick {

	private lateinit var mainClock:AsyncClock
	private var world = ClassicWorld(40, 40)
	private var tickrate = 0L

	private lateinit var manipulator: MapManipulator

	private lateinit var renderer: ShapeRenderer
	private lateinit var camera: OrthographicCamera
	private lateinit var stage: Stage
	private lateinit var menuButton: ImageButton

	private lateinit var elementTable: Table
	private var tableShow: Boolean = true

	private var brushes:HashMap<String, Color> = HashMap()

	lateinit var tooltip: Label
	lateinit var tooltipTable: Table

	override fun create() {
		FontManager.init()
		renderer = ShapeRenderer()
		camera = OrthographicCamera(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
		stage = Stage(ScreenViewport())
		manipulator = MapManipulator(world, camera, stage)

		tooltipTable = Table(Color.BLACK)
		tooltip = Label("", getLabelStyle(Color.WHITE, 10))
		tooltipTable.add(tooltip).pad(2f).fill()


		camera.zoom = if(stage.width > stage.height) (world.height.toFloat()/stage.height) else (world.width.toFloat()/stage.width)
		camera.position.set(world.width/2f, world.height/2f, 0f)
		menuButton = StripeButton(Color.DARK_GRAY, Color.LIGHT_GRAY, 30)
		stage.addActor(menuButton)
		elementTable = Table(Color(0x1f1f1faf))
		elementTable.top().left()

		var count = 0
		world.classes.forEach{
			var color = Color(it.value.getConstructor(Vector2i::class.java).newInstance(Vector2i()).getIdleColor().shl(8)+0xff)
			var color1 = color.cpy()
			color1.a = 0.5f
			brushes.put(it.key, color1)
			var button = ColorButton(color)
			button.addListener(object : ClickListener() {
				var visib = false
				override fun clicked(event: InputEvent?, x: Float, y: Float) {
					super.clicked(event, x, y)
					manipulator.toPlace = it.key
				}

				override fun enter(event: InputEvent?, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
					super.enter(event, x, y, pointer, fromActor)
					visib = true
					tooltipTable.isVisible = true
					tooltip.setText(it.key)
					tooltipTable.pack()
					tooltipTable.setPosition(MathUtils.clamp(Gdx.input.x.toFloat()+10f, 0f, stage.width-tooltipTable.width), MathUtils.clamp(stage.height-Gdx.input.y.toFloat()+10f, 0f, stage.height-tooltipTable.height))
				}

				override fun mouseMoved(event: InputEvent?, x: Float, y: Float): Boolean {
					if(visib)
						tooltipTable.setPosition(MathUtils.clamp(Gdx.input.x.toFloat()+10f, 0f, stage.width-tooltipTable.width), MathUtils.clamp(stage.height-Gdx.input.y.toFloat()+10f, 0f, stage.height-tooltipTable.height))
					return super.mouseMoved(event, x, y)
				}

				override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?) {
					super.exit(event, x, y, pointer, toActor)
					visib = false
					tooltipTable.isVisible = false
				}
			})

			//button.addListener(TextTooltip(it.key))
			elementTable.add(button).pad(3f).size(40f)
			++count
			if(count==4) {
				elementTable.row()
				count = 0
			}

		}

		tooltipTable.isVisible = false

		stage.addActor(elementTable)
		stage.addActor(tooltipTable)

		Gdx.input.inputProcessor = manipulator

		mainClock = AsyncClock(this, 1000)
		mainClock.start()
	}

	private var color = Color()
	private val bound = Color(0.2f, 0.2f, 0.2f, 0.6f)
	private var delta1 = 0f
	override fun render() {
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

		delta1+=Gdx.graphics.deltaTime
		if(delta1>=1f) {
			var fps = Gdx.graphics.framesPerSecond
			var nodes = world.getStateManager().usedNodes
			var tickables = world.entities
			Gdx.graphics.setTitle("LogicDraw (tickrate: $tickrate) (fps: $fps) ($nodes nodes) ($tickables tickables)")
			delta1 = 0f
		}

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
		}else {
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
		var camPosBefore = camera.position.cpy()

		stage.viewport.update(width, height, true)

		menuButton.setPosition(stage.width-menuButton.width-5, stage.height-menuButton.height-5)
		menuButton.setColor(1f, 1f, 1f, if(tableShow) 0.5f else 1f)

		camera.setToOrtho(true, width.toFloat(), height.toFloat())
		camera.position.set(camPosBefore)
		camera.fit(world, stage)

		elementTable.setBounds(if(tableShow) stage.width-200f else stage.width, 0f, 200f, stage.height)
	}

	override fun dispose() {
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

}