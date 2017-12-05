package me.wieku.circuits.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.render.utils.fit
import me.wieku.circuits.world.ClassicWorld

class MapManipulator(val world:ClassicWorld, val camera: OrthographicCamera, val stage: Stage):InputProcessor {

	var toPlace = "Wire"
	private var last = Vector2i(-1, -1)

	override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
		if(stage.touchUp(screenX, screenY, pointer, button)) return true
		return false
	}

	override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
		if(stage.mouseMoved(screenX, screenY)) return true
		return false
	}

	override fun keyTyped(character: Char): Boolean {
		if(stage.keyTyped(character)) return true
		return false
	}

	override fun scrolled(amount: Int): Boolean {
		if(stage.scrolled(amount)) return true
		if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
			var before = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
			camera.zoom = Math.max(0.01f, Math.min(4f, camera.zoom+amount*camera.zoom*0.1f))
			camera.update()
			var after = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
			camera.position.sub(after.sub(before))
			camera.fit(world, stage)
		}
		return false
	}

	override fun keyUp(keycode: Int): Boolean {
		if(stage.keyUp(keycode)) return true
		return false
	}

	override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
		if(stage.touchDragged(screenX, screenY, pointer)) return true
		processTouch(screenX, screenY, pointer, true)
		return false
	}

	override fun keyDown(keycode: Int): Boolean {
		if(stage.keyDown(keycode)) return true
		return false
	}

	override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
		if(stage.touchDown(screenX, screenY, pointer, button)) return true
		println("dwagrgre")
		processTouch(screenX, screenY, pointer, false)
		return false
	}

	private fun processTouch(screenX: Int, screenY: Int, pointer: Int, dragging: Boolean) {
		if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
			camera.position.sub(Gdx.input.getDeltaX(pointer)*camera.zoom, Gdx.input.getDeltaY(pointer)*camera.zoom, 0f)
			camera.fit(world, stage)
		} else {
			var upr = camera.unproject(Vector3(screenX.toFloat(), screenY.toFloat(), 0f))
			var res = Vector2i(upr.x.toInt(), upr.y.toInt())
			if(dragging) {
				if(res == last)
					return
				else last.set(res)
			} else {
				last.set(-1, -1)
			}
			if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
				world.placeElement(res, toPlace)
			} else if(Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
				world.removeElement(res)
			}
		}
	}

}