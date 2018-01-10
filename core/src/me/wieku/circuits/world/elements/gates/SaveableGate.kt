package me.wieku.circuits.world.elements.gates

import me.wieku.circuits.api.element.BasicGate
import me.wieku.circuits.api.element.edit.Copyable
import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.state.State
import me.wieku.circuits.api.world.IWorld
import me.wieku.circuits.save.SaveManager
import me.wieku.circuits.save.Saveable
import me.wieku.circuits.world.ClassicWorld

abstract class SaveableGate(pos: Vector2i): BasicGate(pos), Saveable, Copyable {

	protected var state: State? = null

	override fun onPlace(world: IWorld) {
		super.onPlace(world)
		state = world.getStateManager()()
	}

	override fun onRemove(world: IWorld) {
		setOut(false)
		state!!.unregister()
	}

	override fun setState(state: State, axis: Axis) {}

	override fun getState(axis: Axis): State? = state

	override fun getColor(): Int = if (state!= null && state!!.isActiveD()) getActiveColor() else getIdleColor()

	override fun save(manager: SaveManager) {
		manager.putInteger(state!!.id)
	}

	override fun load(world: ClassicWorld, manager: SaveManager) {
		state = world.getStateManager().getState(manager.getInteger())!!
		state!!.register()
	}

	override fun afterLoad(world: ClassicWorld) {
		updateIO(world)
	}

	override fun copyData(): HashMap<String, Any> {
		val map = HashMap<String, Any>()
		map.put("state", state!!.isActive())
		return map
	}

	override fun pasteData(data: HashMap<String, Any>) {
		val bool = data["state"] as Boolean
		println(bool)
		state?.setActive(bool)
		setOut(state!!.isActiveD())
	}

}