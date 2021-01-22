package me.wieku.circuits.save.legacy

import me.wieku.circuits.world.ClassicWorld
import java.io.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object SaveManagers {
	val latest = 2

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
				var version = inputStream.readInt()
				println(version)
				var world = getSaveManager(version).loadMap(inputStream)
				inputStream.close()
				println(("Loading ${file.name} finished!"))
				return world
			}
		}
		throw IOException("Invaild file")
	}

	fun getHeader(file: File) : Array<String>? {
		if(file.exists() && file.extension == "ldmap") {
			var inputStream = DataInputStream(GZIPInputStream(FileInputStream(file)))
			if(inputStream.readUTF() == "LogicDraw Map") {
				var version = inputStream.readInt()
				val array = getSaveManager(version).loadHeader(inputStream)
				if(version < latest) array[0] = array[0] + " (old)"
				inputStream.close()
				return array
			}
		}
		println("[ERROR] Invaild file: ${file.name}")
		return null
	}

	fun saveMap(world: ClassicWorld, file: File) {
		if(file.exists()) file.delete()
		println("Saving map ${file.name}...")
		var outputStream = DataOutputStream(GZIPOutputStream(FileOutputStream(file)))
		outputStream.writeUTF("LogicDraw Map")
		outputStream.writeInt(latest)
		getSaveManager(latest).saveMap(world, outputStream)
		outputStream.close()
		println("Saved!")
	}

	fun saveBlueprint(world: ClassicWorld, file: File) {
		if(file.exists()) file.delete()
		println("Saving blueprint ${file.name}...")
		var outputStream = DataOutputStream(GZIPOutputStream(FileOutputStream(file)))
		outputStream.writeUTF("LogicDraw Blueprint")
		outputStream.writeInt(latest)
		getSaveManager(latest).saveMap(world, outputStream)
		outputStream.close()
		println("Saved!")
	}


	fun getSaveManager(version: Int): SaveManager {
		if(version == 1) {
			return SaveManagerVer01()
		} else if(version == 2) {
			return SaveManagerVer02()
		} else throw IllegalStateException("You have too old version of LogicDraw to open that world")
	}
}