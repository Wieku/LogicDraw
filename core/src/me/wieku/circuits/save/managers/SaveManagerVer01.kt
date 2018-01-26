package me.wieku.circuits.save.managers

import me.wieku.circuits.world.ClassicWorld
import me.wieku.circuits.world.ClassicStateManager
import me.wieku.circuits.save.SaveManager
import me.wieku.circuits.save.Saveable
import me.wieku.circuits.world.ElementRegistry
import java.io.DataInputStream
import java.io.DataOutputStream

class SaveManagerVer01 : SaveManager {

	private lateinit var inputStream: DataInputStream
	private lateinit var outputStream: DataOutputStream

	override fun loadHeader(file: DataInputStream): Array<String> {
		return arrayOf(file.readUTF(), file.readUTF(), file.readUTF())
	}

	override fun loadMap(file: DataInputStream): ClassicWorld {
		inputStream = file
		file.readUTF()
		file.readUTF()
		file.readUTF()
		var name = file.readUTF()
		println("World name: $name")
		var world = ClassicWorld(file.readInt(), file.readInt(), name)

		println("World size: ${world.width}x${world.height}")

		(world.getStateManager() as ClassicStateManager).load(this)

		println("States: ${world.getStateManager().usedNodes}")

		var counter = file.readInt()
		println("Loading $counter elements started...")
		while (counter>0) {
			var element = world.forcePlace(file.readInt(), file.readInt(), file.readUTF())
			if (element is Saveable) {
				(element as Saveable).load(world, this)
			}
			--counter
		}

		println("Loading elements finished, finalizing...")

		for (x in 0 until world.width) {
			for (y in 0 until world.height) {
				var element = world[x, y]
				//if (element != null && element is Saveable) {
					element?.afterLoad(world)
				//}
			}
		}
		return world
	}

	override fun saveMap(world: ClassicWorld, file: DataOutputStream) {
		outputStream = file
		file.writeUTF(world.name)
		file.writeInt(world.width)
		file.writeInt(world.height)

		(world.getStateManager() as ClassicStateManager).save(this)

		var counter = 0
		for (x in 0 until world.width) {
			for (y in 0 until world.height) {
				if (world[x, y] != null) ++counter
			}
		}

		file.writeInt(counter)
		for (x in 0 until world.width) {
			for (y in 0 until world.height) {
				var element = world[x, y]
				if (element != null) {
					file.writeInt(x)
					file.writeInt(y)
					file.writeUTF(ElementRegistry.names[element.javaClass]!!)

					if(element is Saveable)
						(element as Saveable).save(this)
				}
			}
		}
	}

	override fun putBoolean(value: Boolean) {
		outputStream.writeByte(if(value) 1 else 0)
	}

	override fun putByte(value: Byte) {
		outputStream.writeByte(value.toInt())
	}

	override fun putInteger(value: Int) {
		outputStream.writeInt(value)
	}

	override fun putString(value: String) {
		outputStream.writeUTF(value)
	}

	override fun getBoolean(): Boolean = inputStream.readByte() == (1).toByte()

	override fun getByte(): Byte = inputStream.readByte()

	override fun getInteger(): Int = inputStream.readInt()

	override fun getString(): String = inputStream.readUTF()

	override fun getVersion(): Int = 1
}