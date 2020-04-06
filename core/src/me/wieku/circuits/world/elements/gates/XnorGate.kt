package me.wieku.circuits.world.elements.gates

import me.wieku.circuits.api.math.Vector2i

class XnorGate(pos: Vector2i) : SaveableGate(pos) {

    override fun update(tick: Long) {
        val calc = inputsAll.isXORActive()

        state!!.setActiveU(!calc)
        setOut(!calc)
    }

    override fun getIdleColor(): Int = 0x880E4F

    override fun getActiveColor(): Int = 0xAD1457

}