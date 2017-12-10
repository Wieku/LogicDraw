package me.wieku.circuits

import com.badlogic.gdx.Game
import me.wieku.circuits.render.screens.WorldCreator

import me.wieku.circuits.render.utils.*

object Main : Game() {

	override fun create() {
		FontManager.init()

		/*var world:ClassicWorld
		try {
			world = SaveManagers.loadMap(File("test.ldmap"))
		} catch (e: Exception) {
			world = ClassicWorld(100, 100, "Test")
		}*/

		setScreen(WorldCreator())
	}

}