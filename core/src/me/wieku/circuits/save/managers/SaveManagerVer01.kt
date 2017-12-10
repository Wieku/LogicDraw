package me.wieku.circuits.save.managers

import me.wieku.circuits.world.ClassicWorld
import me.wieku.circuits.world.ClassicStateManager
import me.wieku.circuits.save.SaveManager
import me.wieku.circuits.save.Saveable
import java.io.DataInputStream
import java.io.DataOutputStream

class SaveManagerVer01 : SaveManager {

	private lateinit var inputStream: DataInputStream
	private lateinit var outputStream: DataOutputStream

	override fun loadMap(file: DataInputStream): ClassicWorld {
		var world = ClassicWorld(file.readInt(), file.readInt())

		(world.getStateManager() as ClassicStateManager).load(this)

		while (file.available() > 0) {
			var element = world.forcePlace(file.readInt(), file.readInt(), file.readUTF())
			if (element is Saveable) {
				(element as Saveable).load(this)
			}
		}

		for (x in 0 until world.width) {
			for (y in 0 until world.height) {
				var element = world[x, y]
				if (element != null && element is Saveable) {
					(element as Saveable).afterLoad(world)
				}
			}
		}

		return world
	}

	override fun saveMap(world: ClassicWorld, file: DataOutputStream) {
		file.writeInt(world.width)
		file.writeInt(world.height)

		(world.getStateManager() as ClassicStateManager).save(this)

		for (x in 0 until world.width) {
			for (y in 0 until world.height) {
				var element = world[x, y]
				if (element != null && element is Saveable) {
					(element as Saveable).save(this)
				}
			}
		}
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

	override fun getByte(): Byte = inputStream.readByte()

	override fun getInteger(): Int = inputStream.readInt()

	override fun getString(): String = inputStream.readUTF()

	override fun getVersion(): Int = 1
}