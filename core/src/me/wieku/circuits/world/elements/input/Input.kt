package me.wieku.circuits.world.elements.input

import me.wieku.circuits.api.element.BasicInput
import me.wieku.circuits.api.element.BasicWire
import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.state.State
import me.wieku.circuits.api.world.IWorld
import me.wieku.circuits.save.SaveManager
import me.wieku.circuits.save.Saveable
import me.wieku.circuits.world.ClassicWorld
import java.util.*

open class Input(pos: Vector2i):BasicInput(pos), Saveable {

	private lateinit var state: State
	private val inputs = ArrayList<State>()

	override fun isActive(): Boolean {
		state.setActive(false)
		if(inputs.isNotEmpty()) {
			for(i in 0 until inputs.size) {
				if(inputs[i].isActive()) {
					state.setActive(true)
					return true
				}
			}
		}
		return false
	}

	override fun onPlace(world: IWorld) {
		state = world.getStateManager()()
		updateI(world)
		world.updateNeighboursOf(pos)
	}

	override fun onNeighbourChange(position: Vector2i, world: IWorld) {
		updateI(world)
	}

	private fun updateI(world: IWorld) {
		inputs.clear()

		world.getNeighboursOf(this) {
			when(it) {
				is BasicWire -> {
					var intSt = it.getState(Axis.getAxis(getPosition(), it.getPosition()))
					if(intSt != null)
						inputs += intSt
				}
			}
		}
	}

	override fun onRemove(world: IWorld) {
		state.unregister()
		world.updateNeighboursOf(pos)
	}

	override fun getIdleColor(): Int = 0x01579B

	override fun getActiveColor(): Int = 0x0277BD

	override fun getColor(): Int = if(isActive()) getActiveColor() else getIdleColor()

	override fun setState(state: State, axis: Axis) {}

	override fun getState(axis: Axis): State = state

	override fun save(manager: SaveManager) {
		manager.putInteger(state.id)
	}

	override fun load(world: ClassicWorld, manager: SaveManager) {
		state = world.getStateManager().getState(manager.getInteger())!!
		state.register()
	}

	override fun afterLoad(world: ClassicWorld) {
		updateI(world)
	}
}