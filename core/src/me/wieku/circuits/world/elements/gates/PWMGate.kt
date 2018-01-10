package me.wieku.circuits.world.elements.gates

import com.badlogic.gdx.math.MathUtils
import me.wieku.circuits.api.element.edit.Editable
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.save.SaveManager
import me.wieku.circuits.world.ClassicWorld
import java.util.HashMap

class PWMGate(pos: Vector2i): SaveableGate(pos), Editable {

	@Editable.Spinner("Period", intArrayOf(1, 1, 100000, 1))
	private var period = 500

	@Editable.Spinner("Duty cycle", intArrayOf(50, 0, 100, 1))
	private var duty = 50

	private var counter = 0

	override fun update(tick: Long) {
		var calc = false
		for(i in 0 until inputs.size)
			calc = calc || inputs[i].isActive()

		if(calc) {
			state!!.setActive(false)
			counter = 0
		} else {
			counter += 1
			counter = MathUtils.clamp(counter, 0, period)

			val ticks = period * duty / 100

			if(counter <= ticks) {
				if(!state!!.isActive())
					state!!.setActive(true)
			} else {
				if(state!!.isActive())
					state!!.setActive(false)
			}

			if (counter == period) {
				counter = 0
			}
		}

		setOut(state!!.isActiveD())
	}

	override fun getIdleColor(): Int = 0xAA00FF

	override fun getActiveColor(): Int = 0xD500F9

	override fun load(world: ClassicWorld, manager: SaveManager) {
		super.load(world, manager)
		period = manager.getInteger()
		duty = manager.getInteger()
		counter = manager.getInteger()
	}

	override fun save(manager: SaveManager) {
		super.save(manager)
		manager.putInteger(period)
		manager.putInteger(duty)
		manager.putInteger(counter)
	}

	override fun copyData(): HashMap<String, Any> {
		val map =  super.copyData()
		map.put("period", period)
		map.put("duty", duty)
		map.put("counter", counter)
		return map
	}

	override fun pasteData(data: HashMap<String, Any>) {
		super.pasteData(data)
		period = data["period"] as Int
		duty = data["duty"] as Int
		counter = data["counter"] as Int
	}

}