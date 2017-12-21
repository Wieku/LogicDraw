package me.wieku.circuits.world.elements.gates

import com.badlogic.gdx.math.MathUtils
import me.wieku.circuits.api.element.BasicInput
import me.wieku.circuits.api.element.BasicWire
import me.wieku.circuits.api.element.edit.Editable
import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.world.IWorld
import me.wieku.circuits.save.SaveManager
import me.wieku.circuits.world.ClassicWorld
import me.wieku.circuits.world.elements.input.Controller
import java.util.*

class DelayGate(pos: Vector2i): SaveableGate(pos), Editable {

	@Editable.Spinner("Delay", intArrayOf(1, 1, 10000, 1))
	private var delay = 500

	private var counter = 0

	override fun update(tick: Long) {
		var calc = false
		for(i in 0 until inputs.size)
			calc = calc || inputs[i].isActive()

		counter += if(calc) 1 else -1
		counter = MathUtils.clamp(counter, 0, delay)

		if(counter == 0) {
			if(state!!.isActive()) {
				state!!.setActive(false)
			}
		} else if (counter == delay) {
			if(!state!!.isActive()) {
				state!!.setActive(true)
			}
		}

		setOut(state!!.isActiveD())
	}

	override fun getIdleColor(): Int = 0x827717

	override fun getActiveColor(): Int = 0x9E9D24

	override fun load(world: ClassicWorld, manager: SaveManager) {
		super.load(world, manager)
		delay = manager.getInteger()
		counter = manager.getInteger()
	}

	override fun save(manager: SaveManager) {
		super.save(manager)
		manager.putInteger(delay)
		manager.putInteger(counter)
	}
}