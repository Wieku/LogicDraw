package me.wieku.circuits.render.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.utils.Scaling
import com.kotcrab.vis.ui.widget.VisWindow
import ktx.vis.window
import me.wieku.circuits.render.scene.Drawable
import me.wieku.circuits.render.scene.onClickS
import me.wieku.circuits.utils.Version

object About {

	private var window: VisWindow? = null

	fun showAboutWindow(stage: Stage) {
		if (window == null || !stage.actors.contains(window)) {
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
			window!!.touchable = Touchable.enabled
			window!!.addListener(object: InputListener() {
				override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
					super.touchDown(event, x, y, pointer, button)
					return true
				}


			})
			stage.addActor(window!!.fadeIn())
		}
	}

}