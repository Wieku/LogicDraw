package me.wieku.circuits

import com.badlogic.gdx.Game
import me.wieku.circuits.render.screens.Editor

import me.wieku.circuits.render.utils.*
import me.wieku.circuits.world.ClassicWorld

class Main : Game() {

	override fun create() {
		FontManager.init()
		setScreen(Editor(ClassicWorld(1024, 1024)))
	}

}