package me.wieku.circuits.save

import me.wieku.circuits.api.world.IWorld

interface Saveable {
	fun save(manager: SaveManager)
	fun load(manager: SaveManager)

	fun afterLoad(world: IWorld)
}