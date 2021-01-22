package me.wieku.circuits.world.elements.gates

import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.save.legacy.SaveManager
import me.wieku.circuits.world.ClassicWorld

class TFFGate(pos: Vector2i) : SaveableGate(pos) {

    private var toUpdate = true

    override fun update(tick: Long) {
        val calc = inputs.isActive()
        val calc2 = controllers.isActive()

        if (calc) {
            if (toUpdate) {
                if (!calc2)
                    state!!.setActiveU(!state!!.isActive())
                toUpdate = false
            }
        } else {
            toUpdate = true
        }

        if (calc2) {
            state!!.setActiveU(false)
        }

        setOut(state!!.isActiveD())
    }

    override fun getIdleColor(): Int = 0x311B92

    override fun getActiveColor(): Int = 0x4527A0

    override fun load(world: ClassicWorld, manager: SaveManager) {
        super.load(world, manager)
        toUpdate = manager.getBoolean()
    }

    override fun save(manager: SaveManager) {
        super.save(manager)
        manager.putBoolean(toUpdate)
    }
}