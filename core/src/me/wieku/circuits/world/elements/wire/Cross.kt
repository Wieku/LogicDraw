package me.wieku.circuits.world.elements.wire

import me.wieku.circuits.api.element.BasicWire
import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.state.State
import me.wieku.circuits.api.world.IWorld
import me.wieku.circuits.save.SaveManager
import me.wieku.circuits.save.Saveable
import me.wieku.circuits.world.ClassicWorld

open class Cross(pos: Vector2i): BasicWire(pos), Saveable {

	var stateH:State? = null
	var stateV:State? = null

	override fun onPlace(world: IWorld) {
		var list = world.getNeighboursOf(this)
		if(list.isNotEmpty()) {
			var ix = -1
			var sizex = 0
			var iy = -1
			var sizey = 0

			for(i in 0 until list.size) {
				if(list[i] !is BasicWire) continue
				val ax = Axis.getAxis(pos, list[i].getPosition())
				val hol = list[i].getState(ax)!!.holders
				if(ax == Axis.HORIZONTAL && hol > sizex) {
					ix = i
					sizex = hol
				} else if(ax == Axis.VERTICAL && hol > sizey) {
					iy = i
					sizey = hol
				}

			}
			stateH = if(ix >= 0) {
				list[ix].getState(Axis.getAxis(pos, list[ix].getPosition()))!!
			} else {
				world.getStateManager().createState()
			}

			stateV = if(iy >= 0) {
				list[iy].getState(Axis.getAxis(pos, list[iy].getPosition()))!!
			} else {
				world.getStateManager().createState()
			}
			stateH!!.register()
			stateV!!.register()
			world.updateNeighboursOf(pos)
		} else {
			stateH = world.getStateManager().createState()
			stateV = world.getStateManager().createState()
			stateH!!.register()
			stateV!!.register()
		}
		world.elementStateUpdated(pos)
	}

	override fun onNeighbourChange(position: Vector2i, world: IWorld) {
		var axis = Axis.getAxis(pos, position)
		if(axis==Axis.HORIZONTAL) {
			if (world.getElement(position) == null) {
				stateH!!.unregister()
				stateH = world.getStateManager().createState()
				stateH!!.register()
				world.updateNeighboursOf(pos)
				world.elementStateUpdated(pos)
			} else if (world.getElement(position) is BasicWire){
				var stateU = world.getElement(position)!!.getState(axis)
				if(stateH != stateU) {
					stateH!!.unregister()
					stateH = stateU!!
					stateH!!.register()
					world.updateNeighboursOf(pos)
					world.elementStateUpdated(pos)
				}
			}
		} else if(axis==Axis.VERTICAL) {
			if (world.getElement(position) == null) {
				stateV!!.unregister()
				stateV = world.getStateManager().createState()
				stateV!!.register()
				world.updateNeighboursOf(pos)
				world.elementStateUpdated(pos)
			} else if (world.getElement(position) is BasicWire){
				var stateU = world.getElement(position)!!.getState(axis)
				if(stateV != stateU) {
					stateV!!.unregister()
					stateV = stateU!!
					stateV!!.register()
					world.updateNeighboursOf(pos)
					world.elementStateUpdated(pos)
				}
			}
		}

	}

	override fun onRemove(world: IWorld) {
		stateH!!.unregister()
		stateV!!.unregister()
		world.updateNeighboursOf(pos)
	}

	override fun getIdleColor(): Int = 0x757575

	override fun getActiveColor(): Int = 0x9E9E9E

	open fun getActiveColor2(): Int = 0xBDBDBD

	override fun getColor(): Int {
		val hNull = stateH != null
		val vNull = stateV != null
		if(hNull && vNull && stateH!!.isActive() && stateV!!.isActive()) {
			return getActiveColor2()
		} else if((hNull && stateH!!.isActive()) || (vNull && stateV!!.isActive())) {
			return getActiveColor()
		}
		return getIdleColor()
	}

	override fun setState(state: State, axis: Axis) {}

	override fun getState(axis: Axis): State? = if(axis == Axis.HORIZONTAL) stateH else stateV

	override fun save(manager: SaveManager) {
		manager.putInteger(stateH!!.id)
		manager.putInteger(stateV!!.id)
	}

	override fun load(world: ClassicWorld, manager: SaveManager) {
		stateH = world.getStateManager().getState(manager.getInteger())!!
		stateH!!.register()
		stateV = world.getStateManager().getState(manager.getInteger())!!
		stateV!!.register()
		world.elementStateUpdated(pos)
	}

	override fun afterLoad(world: IWorld) {}
}