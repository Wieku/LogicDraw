package me.wieku.circuits.world.elements.gates

import me.wieku.circuits.api.element.BasicGate
import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.state.State
import me.wieku.circuits.api.world.IWorld
import me.wieku.circuits.save.SaveManager
import me.wieku.circuits.save.Saveable
import me.wieku.circuits.world.ClassicWorld

abstract class SaveableGate(pos: Vector2i): BasicGate(pos), Saveable {

	protected lateinit var state: State

	override fun onPlace(world: IWorld) {
		super.onPlace(world)
		state = world.getStateManager()()
	}

	override fun onRemove(world: IWorld) {
		setOut(false)
		state.unregister()
	}

	override fun setState(state: State, axis: Axis) {}

	override fun getState(axis: Axis): State = state

	override fun save(manager: SaveManager) {
		manager.putInteger(state.id)
	}

	override fun load(world: ClassicWorld, manager: SaveManager) {
		state = world.getStateManager().getState(manager.getInteger())!!
		state.register()
	}

	override fun afterLoad(world: IWorld) {
		updateIO(world)
	}
}