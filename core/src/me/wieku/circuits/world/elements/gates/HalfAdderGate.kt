package me.wieku.circuits.world.elements.gates

//import me.wieku.circuits.api.element.BasicInput
import me.wieku.circuits.api.element.BasicOutput
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.world.IWorld

class HalfAdderGate(pos: Vector2i) : SaveableGate(pos) {

    private var carryOut: BasicOutput? = null

    override fun update(tick: Long) {
        val calcX = inputsAll.isXORActive()
        val calcA = inputsAll.isAllActive()

        state!!.setActiveU(calcX)

        carryOut?.setOut(calcA)

        setOut(state!!.isActiveD())
    }

    override fun getIdleColor(): Int = 0x1A237E

    override fun getActiveColor(): Int = 0x283593

    override fun updateIO(world: IWorld) {
        super.updateIO(world)
        carryOut = null

        world.getNeighboursOf(this) {
            when (it) {
                is BasicOutput -> {
                    carryOut = it
                }
            }
        }
    }

}