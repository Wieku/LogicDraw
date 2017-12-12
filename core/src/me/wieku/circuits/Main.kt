package me.wieku.circuits

import com.badlogic.gdx.Game
import me.wieku.circuits.api.world.clock.AsyncClock
import me.wieku.circuits.render.screens.WorldCreator

import me.wieku.circuits.render.utils.*

object Main : Game() {

	override fun create() {
		FontManager.init()

		Runtime.getRuntime().addShutdownHook(object: Thread(){
			override fun run() {
				AsyncClock.dispose()
			}
		})

		setScreen(WorldCreator())
	}

}