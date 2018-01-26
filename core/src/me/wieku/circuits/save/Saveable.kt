package me.wieku.circuits.save

import me.wieku.circuits.world.ClassicWorld

interface Saveable {
	fun save(manager: SaveManager)
	fun load(world: ClassicWorld, manager: SaveManager)

	//fun afterLoad(world: ClassicWorld)
}