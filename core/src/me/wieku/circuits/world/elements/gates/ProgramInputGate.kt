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

class ProgramInputGate(pos: Vector2i) : SaveableGate(pos), Editable {

	@Editable.Hex("Program")
	private var bytes: ByteArray = ByteArray(1)
		set(value) {
			field = value
			index = -1
		}

	private var index = -1

	private var toUpdate = true
	private var toUpdate2 = true
	protected val controllers = ArrayList<BasicInput>()

	override fun update(tick: Long) {
		var calc = false
		for (i in 0 until controllers.size)
			calc = calc || controllers[i].isActive()

		if (calc) {
			if (toUpdate) {
				index = -1
			}
		} else {
			toUpdate = true
		}


		var calc2 = false
		for (i in 0 until inputs.size)
			calc2 = calc2 || inputs[i].isActive()


		if (calc2) {
			if (toUpdate2) {
				state!!.setActive(nextBit())
				toUpdate2 = false
			}
		} else {
			toUpdate2 = true
		}

		setOut(state!!.isActive())
	}

	fun nextBit(): Boolean {
		index++
		val byteIndex = index / 8
		var bit = 0
		if (byteIndex < bytes.size) {
			bit = bytes[byteIndex].toInt().ushr(7 - (index % 8)).and(1)
		}
		return bit > 0
	}

	override fun getIdleColor(): Int = 0x21274F

	override fun getActiveColor(): Int = 0x424A64

	override protected fun updateIO(world: IWorld) {
		inputs.clear()
		outputs.clear()
		controllers.clear()

		world.getNeighboursOf(this) {
			when (it) {
				is BasicInput -> {
					if (it is Controller) controllers += it else inputs += it
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
		toUpdate2 = manager.getByte() == 1.toByte()
		index = manager.getInteger()
		bytes = ByteArray(manager.getInteger())
		for (i in 0 until bytes.size) {
			bytes[i] = manager.getByte()
		}
	}

	override fun save(manager: SaveManager) {
		super.save(manager)
		manager.putByte(if (toUpdate) 1 else 0)
		manager.putByte(if (toUpdate2) 1 else 0)
		manager.putInteger(index)
		manager.putInteger(bytes.size)
		for (byte in bytes) {
			manager.putByte(byte)
		}
	}
}