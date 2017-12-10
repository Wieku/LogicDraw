package me.wieku.circuits

import com.badlogic.gdx.Game
import me.wieku.circuits.render.screens.WorldCreator

import me.wieku.circuits.render.utils.*

object Main : Game() {

	override fun create() {
		FontManager.init()

		setScreen(WorldCreator())
	}

}