package me.wieku.circuits.desktop

import com.badlogic.gdx.Files
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import me.wieku.circuits.Main

fun main(args: Array<String>) {
	val config = LwjglApplicationConfiguration()
	config.width = 1024
	config.height = 768
	config.title = "LogicDraw"
	config.addIcon("assets/logo/logo32.png", Files.FileType.Internal)
	LwjglApplication(Main(), config)
}
