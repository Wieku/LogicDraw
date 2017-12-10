package me.wieku.circuits.save

import me.wieku.circuits.save.managers.SaveManagerVer01
import me.wieku.circuits.world.ClassicWorld
import sun.plugin.dom.exception.InvalidStateException
import java.io.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

val latest = 1

fun loadMap(file: File) : ClassicWorld {
	if(file.exists() && file.extension == "ldmap") {
		var inputStream = DataInputStream(GZIPInputStream(FileInputStream(file)))
		if(inputStream.readUTF() == "LogicDraw Map") {
			return getSaveManager(inputStream.readInt()).loadMap(inputStream)
		}
	}
	throw IOException("Invaild file")
}

fun saveMap(world: ClassicWorld, file: File) {
	var outputStream = DataOutputStream(GZIPOutputStream(FileOutputStream(file)))
	outputStream.writeUTF("LogicDraw Map")
	outputStream.writeInt(latest)
	getSaveManager(latest).saveMap(world, outputStream)
}


fun getSaveManager(version: Int): SaveManager {
	if(version == 1) {
		return SaveManagerVer01()
	} else throw InvalidStateException("You have too old version of LogicDraw to open that world")
}