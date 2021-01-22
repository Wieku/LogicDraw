package me.wieku.circuits.world.elements.gates

import me.wieku.circuits.api.element.BasicGate
import me.wieku.circuits.api.element.edit.Copyable
import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.state.State
import me.wieku.circuits.api.world.IWorld
import me.wieku.circuits.save.legacy.SaveManager
import me.wieku.circuits.save.legacy.Saveable
import me.wieku.circuits.world.ClassicWorld

abstract class SaveableGate(pos: Vector2i): BasicGate(pos), Saveable, Copyable {

	protected var state: State? = null

	override fun onPlace(world: IWorld) {
		super.onPlace(world)
		state = world.getStateManager().createState()
		world.elementStateUpdated(pos)
	}

	override fun onRemove(world: IWorld) {
		setOut(false)
		state!!.unregister()
		super.onRemove(world)
	}

	override fun setState(state: State, axis: Axis) {}

	override fun getState(axis: Axis): State? = state

	override fun getColor(): Int = if (state!= null && state!!.isActive()) getActiveColor() else getIdleColor()

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

	override fun updateIO(world: IWorld) {
		if (state != null && state!!.isActiveD()) {
			outputs.setActive(false)
		}

		super.updateIO(world)

		if (state != null && state!!.isActiveD()) {
			outputs.setActive(true)
		}
	}

	override fun setOut(value: Boolean) {
		if (value != state!!.isActive()) {
			super.setOut(value)
		}
	}

	override fun copyData(): HashMap<String, Any> {
		val map = HashMap<String, Any>()
		map.put("state", state!!.activeNum)
		return map
	}

	override fun pasteData(data: HashMap<String, Any>) {
		val num = data["state"] as Int
		state?.activeNum = num
		if (state!!.isActiveD()) {
			setOut(true)
		}
	}

}