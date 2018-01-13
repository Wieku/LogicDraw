package me.wieku.circuits.world.elements.wire

import me.wieku.circuits.api.element.BasicWire
import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.state.State
import me.wieku.circuits.api.world.IWorld
import me.wieku.circuits.save.SaveManager
import me.wieku.circuits.save.Saveable
import me.wieku.circuits.world.ClassicWorld

open class Wire(pos: Vector2i): BasicWire(pos), Saveable {

	var state:State? = null

	override fun onPlace(world: IWorld) {
		var list = world.getNeighboursOf(this)
		if(list.isNotEmpty()) {
			var ix = -1
			var size = 0
			for(i in 0 until list.size) {
				if(list[i] !is BasicWire) continue
				var hol = list[i].getState(Axis.getAxis(pos, list[i].getPosition()))!!.holders
				if(hol > size) {
					ix = i
					size = hol
				}
			}
			state = if(ix >= 0) {
				list[ix].getState(Axis.getAxis(pos, list[ix].getPosition()))!!
			} else {
				world.getStateManager()()
			}
			state!!.register()
			world.updateNeighboursOf(pos)
		} else {
			state = world.getStateManager()()
			state!!.register()
		}

	}

	override fun onNeighbourChange(position: Vector2i, world: IWorld) {
		if (world.getElement(position) == null) {
			state!!.unregister()
			state = world.getStateManager()()
			state!!.register()
			world.updateNeighboursOf(pos)
		} else if (world.getElement(position) is BasicWire){
			var stateU = world.getElement(position)!!.getState(Axis.getAxis(pos, position))
			if(state != stateU) {
				state!!.unregister()
				state = stateU!!
				state!!.register()
				world.updateNeighboursOf(pos)
			}
		}
	}

	override fun onRemove(world: IWorld) {
		state!!.unregister()
		world.updateNeighboursOf(pos)
	}

	override fun getIdleColor(): Int = 0x7F0000

	override fun getActiveColor(): Int = 0xD50000

	override fun getColor(): Int = if(state != null && state!!.isActive()) getActiveColor() else getIdleColor()

	override fun setState(state: State, axis: Axis) {}

	override fun getState(axis: Axis): State? = state

	override fun save(manager: SaveManager) {
		manager.putInteger(state!!.id)
	}

	override fun load(world: ClassicWorld, manager: SaveManager) {
		state = world.getStateManager().getState(manager.getInteger())!!
		state!!.register()
	}

	override fun afterLoad(world: ClassicWorld) {}
}