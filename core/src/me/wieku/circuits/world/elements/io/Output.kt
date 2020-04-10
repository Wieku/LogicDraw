package me.wieku.circuits.world.elements.io

import me.wieku.circuits.api.element.BasicOutput
import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.state.State
import me.wieku.circuits.api.world.IWorld
import me.wieku.circuits.save.SaveManager
import me.wieku.circuits.save.Saveable
import me.wieku.circuits.world.ClassicWorld

open class Output(pos: Vector2i): BasicOutput(pos), Saveable {

	private var state: State? = null

	override fun onPlace(world: IWorld) {
		state = world.getStateManager().createState()
		updateIO(world)
		world.updateNeighboursOf(pos)
		world.elementStateUpdated(pos)
	}

	override fun onNeighbourChange(position: Vector2i, world: IWorld) {
		updateIO(world)
	}

	override fun onRemove(world: IWorld) {
		world.updateNeighboursOf(pos)
	}

	override fun setOut(value: Boolean) {
		if (state!!.isActive() != value) {
			state!!.setActiveU(value)
			super.setOut(value)
		}
	}

	override fun getIdleColor(): Int = 0xAEEA00

	override fun getActiveColor(): Int = 0xC6FF00

	override fun getColor(): Int = if(state != null && state!!.isActive()) getActiveColor() else getIdleColor()

	override fun setState(state: State, axis: Axis) {}

	override fun getState(axis: Axis): State? = state

	override fun save(manager: SaveManager) {
		manager.putInteger(state!!.id)
	}

	override fun load(world: ClassicWorld, manager: SaveManager) {
		state = world.getStateManager().getState(manager.getInteger())!!
		state!!.register()
		world.elementStateUpdated(pos)
	}

	override fun afterLoad(world: IWorld) {
		updateIO(world)
	}
}