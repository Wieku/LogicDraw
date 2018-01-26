package me.wieku.circuits.world.elements.gates

import me.wieku.circuits.api.element.BasicInput
import me.wieku.circuits.api.element.BasicOutput
import me.wieku.circuits.api.element.BasicWire
import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.world.IWorld
import me.wieku.circuits.world.elements.io.Controller

class HalfAdderGate(pos: Vector2i): SaveableGate(pos) {

	private var carryOut: BasicOutput? = null

	override fun update(tick: Long) {
		val calcX = inputs.isXORActive()
		val calcA = inputs.isAllActive()

		state!!.setActiveU(calcX)

		carryOut?.setOut(calcA)

		setOut(state!!.isActiveD())
	}

	override fun getIdleColor(): Int = 0x1A237E

	override fun getActiveColor(): Int = 0x283593

	override protected fun updateIO(world: IWorld) {
		inputs.clear()
		outputs.clear()
		carryOut = null

		world.getNeighboursOf(this) {
			when(it) {
				is BasicInput -> {
					inputs += it
				}
				is BasicWire -> {
					outputs += it.getState(Axis.getAxis(getPosition(), it.getPosition()))!!
				}
				is BasicOutput -> {
					carryOut = it
				}
			}
		}
	}

}