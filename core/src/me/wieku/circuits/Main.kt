package me.wieku.circuits

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.DragListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.world.clock.AsyncClock
import me.wieku.circuits.api.world.clock.Updatable
import me.wieku.circuits.render.utils.ColorButton
import me.wieku.circuits.render.utils.StripeButton
import me.wieku.circuits.render.utils.Table
import me.wieku.circuits.world.ClassicWorld

class Main : ApplicationAdapter(), Updatable.ByTick {

	lateinit var ticker:AsyncClock
	var world = ClassicWorld(10, 10)
	var time = System.nanoTime()
	var tick:Long = 0
	override fun update(value: Long) {
		world.update(value)
		if(System.nanoTime()-time >= 1000000000) {
			println(value-tick)
			tick = value
			time = System.nanoTime()
		}
	}

	lateinit var renderer: ShapeRenderer
	lateinit var camera: OrthographicCamera
	lateinit var stage: Stage
	lateinit var menuButton: ImageButton
	private var tableShow: Boolean = true
	lateinit var elementTable: Table
	var toPlace:String = "Wire"

	override fun create() {
		renderer = ShapeRenderer()
		camera = OrthographicCamera(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
		stage = Stage(ScreenViewport())
		stage.viewport.update(Gdx.graphics.width, Gdx.graphics.height, true)
		menuButton = StripeButton(Color.DARK_GRAY, Color.LIGHT_GRAY, 30)
		stage.addActor(menuButton)
		elementTable = Table(Color(0x1f1f1faf))
		elementTable.top().left()

		var count = 0
		world.classes.forEach{
			var button = ColorButton(Color(it.value.getConstructor(Vector2i::class.java).newInstance(Vector2i()).getIdleColor().shl(8)+0xff))
			button.addListener(object : ClickListener() {
				override fun clicked(event: InputEvent?, x: Float, y: Float) {
					super.clicked(event, x, y)
					toPlace = it.key
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
		stage.addActor(elementTable)

		/*world.placeElement(Vector2i(1, 5), "Wire")
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
		world.placeElement(Vector2i(3, 2), "Nor")*/

		ticker = AsyncClock(this, 10)
		ticker.start()

		Gdx.input.inputProcessor = stage

		stage.addListener(object: InputListener(){
			override fun scrolled(event: InputEvent?, x: Float, y: Float, amount: Int): Boolean {
				if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
					var before = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
					camera.zoom = Math.max(0.01f, Math.min(4f, camera.zoom+amount*camera.zoom*0.1f))
					camera.update()
					var after = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
					camera.position.sub(after.sub(before))
					camera.position.set(Math.max(0f, Math.min(10*40f, camera.position.x)), Math.max(0f, Math.min(10*40f, camera.position.y)), 0f)
				}
				return super.scrolled(event, x, y, amount)
			}
		})

		stage.addListener(object: DragListener() {
			override fun drag(event: InputEvent?, x: Float, y: Float, pointer: Int) {
				if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
					camera.position.sub(Gdx.input.getDeltaX(pointer).toFloat()*camera.zoom, Gdx.input.getDeltaY(pointer).toFloat()*camera.zoom, 0f)
					camera.position.set(Math.max(0f, Math.min(10*40f, camera.position.x)), Math.max(0f, Math.min(10*40f, camera.position.y)), 0f)
				} else {
					var res = camera.unproject(Vector3(x, Gdx.graphics.height-y, 0f))
					var veci = Vector2i(res.x.toInt(), res.y.toInt())
					world.placeElement(veci, toPlace)
				}
				super.drag(event, x, y, pointer)
			}
		})
	}

	var color = Color()
	override fun render() {
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

		camera.update()
		renderer.projectionMatrix = camera.combined
		renderer.begin(ShapeRenderer.ShapeType.Filled)
		renderer.color = Color.BLACK
		renderer.rect(0f, 0f, 10f, 10f)
		for(x in 0 until 10) {
			for(y in 0 until 10) {
				var el = world[x, y]
				if(el != null) {
					color.set((el.getColor().shl(8)) + 0xFF)
					renderer.color = color
					renderer.rect(x.toFloat(), y.toFloat(), 1f, 1f)
				}
			}
		}
		renderer.end()
		stage.act()
		stage.draw()
	}

	override fun resize(width: Int, height: Int) {
		super.resize(width, height)
		stage.viewport.update(width, height, true)
		menuButton.setPosition(stage.width-menuButton.width-5, stage.height-menuButton.height-5)
		menuButton.setColor(1f, 1f, 1f, if(tableShow) 0.5f else 1f)
		camera.setToOrtho(true, width.toFloat(), height.toFloat())
		elementTable.setBounds(if(tableShow) stage.width-200f else stage.width, 0f, 200f, stage.height)
	}

	override fun dispose() {
		renderer.dispose()
	}

}