package me.wieku.circuits.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import me.wieku.circuits.Main

fun main(args: Array<String>) {
	val config = LwjglApplicationConfiguration()
	config.width = 400
	config.height = 400
	LwjglApplication(Main(), config)
}
