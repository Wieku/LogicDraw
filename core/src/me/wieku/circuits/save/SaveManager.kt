package me.wieku.circuits.save

import me.wieku.circuits.world.ClassicWorld
import java.io.DataInputStream
import java.io.DataOutputStream

interface SaveManager {

	fun loadHeader(file: DataInputStream): Array<String>
	fun loadMap(file: DataInputStream): ClassicWorld
	fun saveMap(world: ClassicWorld, file: DataOutputStream)

	fun putBoolean(value: Boolean)
	fun putByte(value: Byte)
	fun putInteger(value: Int)
	fun putLong(value: Long)
	fun putString(value: String)

	fun getBoolean(): Boolean
	fun getByte(): Byte
	fun getInteger(): Int
	fun getLong(): Long
	fun getString(): String

	fun getVersion(): Int
}