package me.wieku.circuits.save

import me.wieku.circuits.save.managers.SaveManagerVer01
import me.wieku.circuits.world.ClassicWorld
import sun.plugin.dom.exception.InvalidStateException
import java.io.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object SaveManagers {
	val latest = 1

	fun loadMap(file: File) : ClassicWorld {
		if(file.exists() && file.extension == "ldmap") {
			var inputStream = DataInputStream(GZIPInputStream(FileInputStream(file)))
			if(inputStream.readUTF() == "LogicDraw Map") {
				println("Loading ${file.name} started...")
				var world = getSaveManager(inputStream.readInt()).loadMap(inputStream)
				inputStream.close()
				println(("Loading ${file.name} finished!"))
				return world
			}
		}
		throw IOException("Invaild file")
	}

	fun loadBlueprint(file: File) : ClassicWorld {
		if(file.exists() && file.extension == "ldbp") {
			var inputStream = DataInputStream(GZIPInputStream(FileInputStream(file)))
			if(inputStream.readUTF() == "LogicDraw Blueprint") {
				println("Loading ${file.name} started...")
				var world = getSaveManager(inputStream.readInt()).loadMap(inputStream)
				inputStream.close()
				println(("Loading ${file.name} finished!"))
				return world
			}
		}
		throw IOException("Invaild file")
	}

	fun getHeader(file: File) : Array<String> {
		if(file.exists() && file.extension == "ldmap") {
			var inputStream = DataInputStream(GZIPInputStream(FileInputStream(file)))
			if(inputStream.readUTF() == "LogicDraw Map") {
				inputStream.readInt()
				var array = arrayOf(inputStream.readUTF(), inputStream.readUTF(), inputStream.readUTF())
				inputStream.close()
				return array
			}
		}
		throw IOException("Invaild file")
	}

	fun saveMap(world: ClassicWorld, file: File) {
		if(file.exists()) file.delete()
		println("Saving ${file.name}...")
		var outputStream = DataOutputStream(GZIPOutputStream(FileOutputStream(file)))
		outputStream.writeUTF("LogicDraw Map")
		outputStream.writeInt(latest)
		outputStream.writeUTF(world.name)
		outputStream.writeUTF(world.width.toString())
		outputStream.writeUTF(world.height.toString())
		getSaveManager(latest).saveMap(world, outputStream)
		outputStream.close()
		println("Saved!")
	}

	fun saveBlueprint(world: ClassicWorld, file: File) {
		if(file.exists()) file.delete()
		println("Saving ${file.name}...")
		var outputStream = DataOutputStream(GZIPOutputStream(FileOutputStream(file)))
		outputStream.writeUTF("LogicDraw Blueprint")
		outputStream.writeInt(latest)
		outputStream.writeUTF(world.name)
		outputStream.writeUTF(world.width.toString())
		outputStream.writeUTF(world.height.toString())
		getSaveManager(latest).saveMap(world, outputStream)
		outputStream.close()
		println("Saved!")
	}


	fun getSaveManager(version: Int): SaveManager {
		if(version == 1) {
			return SaveManagerVer01()
		} else throw InvalidStateException("You have too old version of LogicDraw to open that world")
	}
}