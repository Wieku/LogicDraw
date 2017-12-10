package me.wieku.circuits.desktop

import com.badlogic.gdx.Files
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import me.wieku.circuits.Main
import me.wieku.circuits.utils.Version

fun main(args: Array<String>) {
	val config = LwjglApplicationConfiguration()
	config.width = 1024
	config.height = 768
	config.title = "LogicDraw ${Version.version}"
	config.addIcon("assets/logo/logo32.png", Files.FileType.Internal)
	LwjglApplication(Main, config)
}
