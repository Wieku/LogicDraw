package me.wieku.circuits

import com.badlogic.gdx.Game
import me.wieku.circuits.render.screens.Editor

import me.wieku.circuits.render.utils.*
import me.wieku.circuits.save.SaveManagers
import me.wieku.circuits.world.ClassicWorld
import java.io.File

class Main : Game() {

	override fun create() {
		FontManager.init()

		var world:ClassicWorld
		try {
			world = SaveManagers.loadMap(File("test.ldmap"))
		} catch (e: Exception) {
			world = ClassicWorld(100, 100, "Test")
		}

		setScreen(Editor(world))
	}

	companion object {
		val instance = this
	}

}