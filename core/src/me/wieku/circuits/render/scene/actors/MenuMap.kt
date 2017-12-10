package me.wieku.circuits.render.scene.actors

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Scaling
import com.sun.xml.internal.fastinfoset.util.StringArray
import me.wieku.circuits.Main
import me.wieku.circuits.render.scene.getLabelStyle
import me.wieku.circuits.render.scene.getTextButtonStyle
import me.wieku.circuits.render.scene.getTxHRegion
import me.wieku.circuits.render.scene.getTxWRegion
import me.wieku.circuits.render.screens.Editor
import me.wieku.circuits.save.SaveManagers
import java.io.File

class MenuMap(arr: Array<String>):Table() {

	private var borderTop = Image(getTxHRegion(Color.DARK_GRAY, 2), Scaling.stretchX)
	private var borderBottom = Image(getTxHRegion(Color.DARK_GRAY, 2), Scaling.stretchX)
	private var borderLeft = Image(getTxWRegion(Color.DARK_GRAY, 2), Scaling.stretchY)
	private var borderRight = Image(getTxWRegion(Color.DARK_GRAY, 2), Scaling.stretchY)

	init {

		add(borderTop).fill().colspan(4).row()


		var info = Table()
		var info2 = Table()
		info2.add(Label("World name: ${arr[1]}", getLabelStyle(Color.WHITE, 16))).top().left().expandX().fillX().row()
		info2.add(Label("File name: ${arr[0]}", getLabelStyle(Color.WHITE, 14))).top().left().expandX().fillX().row()
		info2.add(Label("World size: ${arr[2]}x${arr[3]}", getLabelStyle(Color.WHITE, 12))).top().left().expandX().fillX().row()

		info.add(info2).left().expandX()

		var button = TextButton("Load", getTextButtonStyle(Color(0.1f, 0.1f, 0.1f, 1f), Color.WHITE, 14))
		button.addListener(object: ClickListener() {
			override fun clicked(event: InputEvent?, x: Float, y: Float) {
				super.clicked(event, x, y)
				Main.screen = Editor(SaveManagers.loadMap(File("maps/${arr[0]}")))
			}
		})


		info.add(button).fillY().padRight(20f)


		add(borderLeft).fillY()
		add(info).width(1024f * 2 / 3 - 4f).padLeft(5f).padTop(5f).padBottom(5f).left()

		add(borderRight).fillY().row()
		add(borderBottom).colspan(4).fill()

	}

}