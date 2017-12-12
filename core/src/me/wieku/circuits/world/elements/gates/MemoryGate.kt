package me.wieku.circuits.world.elements.gates

import me.wieku.circuits.api.element.BasicInput
import me.wieku.circuits.api.element.BasicWire
import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.world.IWorld
import me.wieku.circuits.save.SaveManager
import me.wieku.circuits.world.ClassicWorld
import me.wieku.circuits.world.elements.input.Controller
import java.util.*

class MemoryGate(pos: Vector2i): SaveableGate(pos) {

	private var toUpdate = true

	protected val controllers = ArrayList<BasicInput>()

	override fun update(tick: Long) {
		var calc = false
		for(i in 0 until controllers.size)
			calc = calc || controllers[i].isActive()


		if(calc) {
			if(toUpdate) {
				var calc2 = false
				for(i in 0 until inputs.size)
					calc2 = calc2 || inputs[i].isActive()
				state.setActive(calc2)
				toUpdate = false
			}
		} else {
			toUpdate = true
		}

		setOut(state.isActive())
	}

	override fun getIdleColor(): Int = 0x37474F

	override fun getActiveColor(): Int = 0x455A64

	override fun getColor(): Int = if (state.isActiveD()) getActiveColor() else getIdleColor()

	override protected fun updateIO(world: IWorld) {
		inputs.clear()
		outputs.clear()

		world.getNeighboursOf(this) {
			when(it) {
				is BasicInput -> {
					if(it is Controller) controllers += it else inputs += it
				}
				is BasicWire -> {
					outputs += it.getState(Axis.getAxis(getPosition(), it.getPosition()))!!
				}
			}
		}
	}

	override fun load(world: ClassicWorld, manager: SaveManager) {
		super.load(world, manager)
		toUpdate = manager.getByte() == 1.toByte()
	}

	override fun save(manager: SaveManager) {
		super.save(manager)
		manager.putByte(if(toUpdate) 1 else 0)
	}
}