package me.wieku.circuits.world.elements.gates

import com.badlogic.gdx.math.MathUtils
import me.wieku.circuits.api.element.BasicInput
import me.wieku.circuits.api.element.BasicWire
import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.world.IWorld
import me.wieku.circuits.save.SaveManager
import me.wieku.circuits.world.ClassicWorld
import me.wieku.circuits.world.elements.input.Controller
import java.util.*

//TODO: Setting delay in UI
class DelayGate(pos: Vector2i): SaveableGate(pos) {

	private var toUpdate = true
	private var delay = 500

	private var counter = 0

	override fun update(tick: Long) {
		var calc = false
		for(i in 0 until inputs.size)
			calc = calc || inputs[i].isActive()

		counter += if(calc) 1 else -1
		counter = MathUtils.clamp(counter, 0, delay)

		if(counter == 0) {
			if(toUpdate) {
				state.setActive(false)
				toUpdate = false
			}
		} else if (counter == 500) {
			if(toUpdate) {
				state.setActive(true)
				toUpdate = false
			}
		} else {
			toUpdate = true
		}

		/*if(calc) {
			if(toUpdate) {
				var calc2 = false
				for(i in 0 until inputs.size)
					calc2 = calc2 || inputs[i].isActive()
				state.setActive(calc2)
				toUpdate = false
			}
		} else {
			toUpdate = true
		}*/

		setOut(state.isActive())
	}

	override fun getIdleColor(): Int = 0x827717

	override fun getActiveColor(): Int = 0x9E9D24

	override fun getColor(): Int = if (state.isActiveD()) getActiveColor() else getIdleColor()

	override fun load(world: ClassicWorld, manager: SaveManager) {
		super.load(world, manager)
		toUpdate = manager.getByte() == 1.toByte()
		delay = manager.getInteger()
		counter = manager.getInteger()
	}

	override fun save(manager: SaveManager) {
		super.save(manager)
		manager.putByte(if(toUpdate) 1 else 0)
		manager.putInteger(delay)
		manager.putInteger(counter)
	}
}