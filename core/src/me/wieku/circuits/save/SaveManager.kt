package me.wieku.circuits.save

import me.wieku.circuits.save.managers.SaveManagerVer01
import me.wieku.circuits.world.ClassicWorld
import java.io.DataInputStream
import java.io.DataOutputStream

interface SaveManager {

	fun loadMap(file: DataInputStream): ClassicWorld
	fun saveMap(world: ClassicWorld, file: DataOutputStream)

	fun putByte(value: Byte)
	fun putInteger(value: Int)
	fun putString(value: String)

	fun getByte(): Byte
	fun getInteger(): Int
	fun getString(): String

	fun getVersion(): Int
}