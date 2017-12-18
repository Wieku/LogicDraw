package me.wieku.circuits.world.elements.gates

import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.world.IWorld
import me.wieku.circuits.save.SaveManager
import me.wieku.circuits.world.ClassicWorld

class StopGate(pos: Vector2i): SaveableGate(pos) {

	private var toUpdate = true
	private var world: ClassicWorld? = null

	override fun update(tick: Long) {
		var calc = false
		for(i in 0 until inputs.size)
			calc = calc || inputs[i].isActive()


		if(calc) {
			if(toUpdate) {
				if(world!!.clock != null){
					world!!.clock!!.stop()
					state.setActive(true)
				}
				toUpdate = false
			}
		} else {
			toUpdate = true
			state.setActive(false)
		}

		setOut(state.isActive())
	}

	override fun getIdleColor(): Int = 0xBF360C

	override fun getActiveColor(): Int = 0xD84315

	override fun getColor(): Int = if (state.isActiveD()) getActiveColor() else getIdleColor()

	override fun onPlace(world: IWorld) {
		this.world = world as ClassicWorld
		super.onPlace(world)
	}

	override fun load(world: ClassicWorld, manager: SaveManager) {
		super.load(world, manager)
		toUpdate = manager.getByte() == 1.toByte()
	}

	override fun save(manager: SaveManager) {
		super.save(manager)
		manager.putByte(if(toUpdate) 1 else 0)
	}

	override fun afterLoad(world: ClassicWorld) {
		super.afterLoad(world)
		this.world = world
	}

}