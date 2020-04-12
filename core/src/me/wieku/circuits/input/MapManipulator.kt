package me.wieku.circuits.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector3
import me.wieku.circuits.api.element.gates.ITickableAlways
import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Rectangle
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.input.event.KeyDownEvent
import me.wieku.circuits.input.event.KeyUpEvent
import me.wieku.circuits.render.scene.fit
import me.wieku.circuits.render.screens.Editor
import me.wieku.circuits.utils.Bresenham
import me.wieku.circuits.world.ClassicWorld
import me.wieku.circuits.world.ElementRegistry
import me.wieku.circuits.world.WorldClipboard

class MapManipulator(val world:ClassicWorld, val camera: OrthographicCamera, val editor: Editor):InputProcessor {

	private enum class STATE {
		NONE,
		DRAWING,
		LINE,
		AXIS_LINE,
		SELECTION,
		PASTE,
		MOVE,
		MOVE_PASTE;
	}

	private var last = Vector2i(-1, -1)
	var position = Vector2i()

	val beginPos = Vector2i()
	val endPos = Vector2i()
	private var beginTMP = Vector2i()
	private var endTMP = Vector2i()

	var rectangle:Rectangle? = null
	var clipboard:WorldClipboard? = null

	var pasteMode = false
	var lineMode = false

	private var lastTooltip = Vector2i(-1, -1)

	private var brushState = STATE.NONE

	var pause = false

	override fun keyDown(keycode: Int): Boolean {
		if(editor.stage.keyDown(keycode)) return true

		if(keycode in Input.Keys.NUM_1..Input.Keys.NUM_9) {
			editor.palette.select(keycode-Input.Keys.NUM_1)
		}

		if(brushState == STATE.SELECTION){
			when (keycode) {
				Input.Keys.C -> makeCopy()
				Input.Keys.X -> makeCut()
				Input.Keys.V -> makePaste()
				//Input.Keys.S -> editor.saveFile()
			}
		}

		if((brushState == STATE.PASTE || brushState == STATE.MOVE_PASTE) && (keycode == Input.Keys.SHIFT_LEFT || keycode == Input.Keys.SHIFT_RIGHT)) {
			brushState = STATE.MOVE_PASTE
			lastMX = Gdx.input.x
			lastMY = Gdx.input.y
			return false
		}

		if(brushState != STATE.NONE) return false

		if(keycode == Input.Keys.CONTROL_LEFT || keycode == Input.Keys.CONTROL_RIGHT) {
			brushState = STATE.SELECTION
		} else if(keycode == Input.Keys.F) {
			if(rectangle != null) {
				if(editor.palette.currentBrush != null)
					world.fill(rectangle!!, editor.palette.currentBrush!!)
			}
		} else if(rectangle != null && keycode == Input.Keys.FORWARD_DEL) {
			world.clear(rectangle!!)
		} else if(keycode == Input.Keys.A) {
			brushState = STATE.LINE
		} else if(keycode == Input.Keys.D) {
			brushState = STATE.AXIS_LINE
		} else if (keycode == Input.Keys.SHIFT_LEFT || keycode == Input.Keys.SHIFT_RIGHT) {
			lastMX = Gdx.input.x
			lastMY = Gdx.input.y
			brushState = STATE.MOVE
		} else {
			world.eventBus.post(KeyDownEvent(world, keycode))
		}

		return false
	}

	override fun keyUp(keycode: Int): Boolean {
		if(editor.stage.keyUp(keycode)) return true
		if(keycode == Input.Keys.A || keycode == Input.Keys.D) {
			if(!Gdx.input.isButtonPressed(Input.Buttons.LEFT) && !Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
				if(keycode == Input.Keys.A && Gdx.input.isKeyPressed(Input.Keys.D)) {
					brushState = STATE.AXIS_LINE
				} else if(Gdx.input.isKeyPressed(Input.Keys.A) && keycode == Input.Keys.D) {
					brushState = STATE.LINE
				} else {
					lineMode = false
					brushState = STATE.NONE
				}
			}
		} else if(brushState == STATE.SELECTION && ((keycode == Input.Keys.CONTROL_LEFT && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) || (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && keycode == Input.Keys.CONTROL_RIGHT))) {
			if (lastTooltip != Vector2i(-1, -1)) {
				editor.tooltip.hide()
				lastTooltip.set(-1, -1)
			}
			if (brushState == STATE.SELECTION && !Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
				brushState = STATE.NONE
			}
		} else if((brushState == STATE.MOVE_PASTE || brushState == STATE.MOVE) && ((keycode == Input.Keys.SHIFT_LEFT && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) || (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && keycode == Input.Keys.SHIFT_RIGHT))) {
			brushState = if(brushState == STATE.MOVE_PASTE) {
				STATE.PASTE
			} else STATE.NONE
		} else {
			world.eventBus.post(KeyUpEvent(world, keycode))
		}

		return false
	}

	override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
		if(editor.stage.mouseMoved(screenX, screenY)) return true
		val vec = camera.unproject(Vector3(screenX.toFloat(), screenY.toFloat(), 0f))
		position.set(vec.x.toInt(), vec.y.toInt())

		if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
			tooltip(position)
		}

		return false
	}


	private fun tooltip(position: Vector2i) {
		var element = world.getElement(position)
		if(element != null) {
			var name = ElementRegistry.names[element.javaClass]!!
			if(lastTooltip != position) {
				var tooltipText = "Element: $name"

				if (element is ITickableAlways) {
					tooltipText += "\nTicks constantly"
				}

				val horizontalState = element.getState(Axis.HORIZONTAL)
				val verticalState = element.getState(Axis.VERTICAL)
				if (horizontalState != null || verticalState != null) {
					tooltipText += "\nState data:"

					if (horizontalState != null) {
						tooltipText += "\n H: {${horizontalState.id}: ${horizontalState.activeNum}}"
					}

					if (verticalState != null) {
						tooltipText += "\n V: {${verticalState.id}: ${verticalState.activeNum}}"
					}
				}

				editor.tooltip.showTooltip(tooltipText)
				lastTooltip.set(position)
			} else {
				editor.tooltip.update()
			}
		} else if(lastTooltip != Vector2i(-1, -1)) {
			editor.tooltip.hide()
			lastTooltip.set(-1, -1)
		}
	}

	override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
		if(editor.stage.touchUp(screenX, screenY, pointer, button)) return true

		if(pause) {
			pause = false
			return false
		}

		var upr = camera.unproject(Vector3(screenX.toFloat(), screenY.toFloat(), 0f))
		endPos.set(upr.x.toInt(), upr.y.toInt()).clamp(0, 0, world.width-1, world.height-1)

		if(brushState == STATE.LINE) {
			lineMode = false
			drawLine(beginPos, endPos, true)
			if(!Gdx.input.isKeyPressed(Input.Keys.A)) {
				brushState = if(Gdx.input.isKeyPressed(Input.Keys.D)) STATE.AXIS_LINE else STATE.NONE
			}
		} else if(brushState == STATE.AXIS_LINE) {
			var angle = endPos.copy().sub(beginPos).angle()

			if(angle >= 315 || angle <=45 || (angle in 135.0..225.0)) {
				endPos.set(endPos.x, beginPos.y)
			} else {
				endPos.set(beginPos.x, endPos.y)
			}
			lineMode = false
			drawLine(beginPos, endPos, true)
			if(!Gdx.input.isKeyPressed(Input.Keys.D)) {
				brushState = if(Gdx.input.isKeyPressed(Input.Keys.A)) STATE.LINE else STATE.NONE
			}
		} else if(brushState == STATE.SELECTION && button == Input.Buttons.LEFT && rectangle != null) {
			calculateSelection(beginTMP.set(beginPos), endTMP.set(endPos))
			rectangle!!.reshape(beginTMP, endTMP)

			if(!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
				brushState = STATE.NONE
			}
		} else if(brushState == STATE.DRAWING) {
			brushState = STATE.NONE
		}

		return false
	}


	private var lastMX = -1
	private var lastMY = -1
	override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
		if(editor.stage.touchDown(screenX, screenY, pointer, button)) return true

		if(button == Input.Buttons.BACK) {
			editor.palette.back()
			return false
		} else if(button == Input.Buttons.FORWARD) {
			editor.palette.forward()
			return false
		}

		lastMX = screenX
		lastMY = screenY
		val upr = camera.unproject(Vector3(screenX.toFloat(), screenY.toFloat(), 0f))
		position.set(upr.x.toInt(), upr.y.toInt()).clamp(0, 0, world.width-1, world.height-1)
		if(pause) return false
		if(brushState == STATE.LINE || brushState == STATE.AXIS_LINE) {
			beginPos.set(upr.x.toInt(), upr.y.toInt()).clamp(0, 0, world.width-1, world.height-1)
			endPos.set(beginPos)
			lineMode = true
		} else if(brushState == STATE.SELECTION) {
			if(button == Input.Buttons.LEFT) {
				beginPos.set(upr.x.toInt(), upr.y.toInt()).clamp(0, 0, world.width-1, world.height-1)
				endPos.set(beginPos)
				rectangle = Rectangle(beginPos, endPos)
			} else if(button == Input.Buttons.RIGHT) {
				val element = world.getElement(Vector2i(upr.x.toInt(), upr.y.toInt()))
				if(element != null)
					editor.editElement(element)
			}
		} else if(brushState == STATE.PASTE) {
			if(button == Input.Buttons.LEFT) {
				world.paste(position.copy().sub(clipboard!!.width/2, clipboard!!.height/2), clipboard!!)
			}
			pasteMode = false
			brushState = STATE.NONE
		} else if(brushState == STATE.NONE) {
			if(rectangle != null) {
				rectangle = null
			} else if(button == Input.Buttons.MIDDLE) {
				val element = world.getElement(Vector2i(upr.x.toInt(), upr.y.toInt()))
				if(element != null) {
					editor.palette.put(ElementRegistry.names[element.javaClass]!!)
				}
			} else {
				brushState = STATE.DRAWING

				last.set(position)

				if(button == Input.Buttons.LEFT) {
					drawLine(last, position, true)
				} else if(button == Input.Buttons.RIGHT) {
					drawLine(last, position, false)
				}
			}
		} else if(brushState == STATE.MOVE || brushState == STATE.MOVE_PASTE) {
			lastMX = screenX
			lastMY = screenY
		}

		return false
	}

	override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
		if(editor.stage.touchDragged(screenX, screenY, pointer)) return true
		val upr = camera.unproject(Vector3(screenX.toFloat(), screenY.toFloat(), 0f))
		position.set(upr.x.toInt(), upr.y.toInt()).clamp(0, 0, world.width-1, world.height-1)
		if(pause) return false
		if(brushState == STATE.LINE || brushState == STATE.AXIS_LINE) {
			endPos.set(upr.x.toInt(), upr.y.toInt()).clamp(0, 0, world.width-1, world.height-1)
			if(brushState == STATE.AXIS_LINE) {
				val angle = endPos.copy().sub(beginPos).angle()

				if(angle >= 315 || angle <=45 || (angle in 135.0..225.0)) {
					endPos.set(endPos.x, beginPos.y)
				} else {
					endPos.set(beginPos.x, endPos.y)
				}
			}
		} else if(brushState == STATE.SELECTION && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
			if(Gdx.input.isButtonPressed(Input.Buttons.LEFT) && rectangle != null) {
				endPos.set(upr.x.toInt(), upr.y.toInt()).clamp(0, 0, world.width-1, world.height-1)

				beginTMP.set(beginPos)
				endTMP.set(endPos)

				calculateSelection(beginTMP, endTMP)

				rectangle!!.reshape(beginTMP, endTMP)
			}

			tooltip(Vector2i(upr.x.toInt(), upr.y.toInt()))
		} else if(brushState == STATE.MOVE || brushState == STATE.MOVE_PASTE) {
			camera.position.sub((screenX-lastMX)*camera.zoom, (screenY-lastMY)*camera.zoom, 0f)
			camera.fit(world, editor.stage)
			lastMX = screenX
			lastMY = screenY
		} else if(brushState == STATE.DRAWING) {
			if(position != last) {
				if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
					drawLine(last, position, true)
				} else if(Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
					drawLine(last, position, false)
				}
				last.set(position)
			}
		}
		return false
	}

	private fun drawLine(from: Vector2i, to: Vector2i, place: Boolean) {
		if(from == to) {
			makeAction(from.x, from.y, place)
		} else {
			Bresenham.iterateFast(from, to) {
				makeAction(it.x, it.y, place)
			}
		}
	}

	private fun makeAction(posx: Int, posy: Int, place: Boolean) {
		if (place) {
			if(editor.palette.currentBrush != null)
				world.placeElement(Vector2i(posx, posy), editor.palette.currentBrush!!)
		} else {
			world.removeElement(Vector2i(posx, posy))
		}
	}

	private fun calculateSelection(beginPosition: Vector2i, endPosition: Vector2i) {
		beginPosition.add(if(endPosition.x < beginPosition.x) 1 else 0, if(endPosition.y < beginPosition.y) 1 else 0)
		endPosition.add(if(endPosition.x < beginPosition.x) 0 else 1, if(endPosition.y < beginPosition.y) 0 else 1)
	}

	fun makeCut() {
		if(rectangle != null) {
			clipboard = WorldClipboard.create(rectangle!!, world)
			world.clear(rectangle!!)
		}
	}

	fun makeCopy() {
		if(rectangle != null) {
			clipboard = WorldClipboard.create(rectangle!!, world)
		}
	}

	fun makePaste() {
		if(clipboard != null) {
			pasteMode = true
			brushState = STATE.PASTE
		}
	}

	override fun keyTyped(character: Char): Boolean {
		if(editor.stage.keyTyped(character)) return true
		return false
	}

	override fun scrolled(amount: Int): Boolean {
		if(editor.stage.scrolled(amount)) return true
		val before = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
		camera.zoom = Math.max(0.01f, Math.min(4f, camera.zoom+amount*camera.zoom*0.1f))
		camera.update()
		val after = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
		camera.position.sub(after.sub(before))
		camera.fit(world, editor.stage)
		return false
	}

}