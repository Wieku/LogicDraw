package me.wieku.circuits.world.elements.gates

//import me.wieku.circuits.api.element.BasicInput
import me.wieku.circuits.api.element.BasicOutput
import me.wieku.circuits.api.element.edit.Editable
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.world.IWorld
import me.wieku.circuits.save.legacy.SaveManager
import me.wieku.circuits.world.ClassicWorld

class ProgramInputGate(pos: Vector2i) : SaveableGate(pos), Editable {

    private var carryOut: BasicOutput? = null

    @Editable.Hex("Program")
    private var bytes: ByteArray = ByteArray(1)
        set(value) {
            field = value
            index = -1
        }

    private var index = -1

    private var toUpdate = true
    private var toUpdate2 = true

    override fun update(tick: Long) {
        val calc = controllers.isActive()

        if (calc) {
            if (toUpdate) {
                index = -1
                state!!.setActiveU(false)
            }
        } else {
            toUpdate = true
        }


        val calc2 = inputs.isActive()

        if (calc2) {
            if (toUpdate2) {
                state!!.setActiveU(nextBit())
                toUpdate2 = false
            }
        } else {
            toUpdate2 = true
        }

        carryOut?.setOut(index >= (bytes.size * 8) - 1)

        setOut(state!!.isActiveD())
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

    override fun load(world: ClassicWorld, manager: SaveManager) {
        super.load(world, manager)
        toUpdate = manager.getBoolean()
        toUpdate2 = manager.getBoolean()
        index = manager.getInteger()
        bytes = ByteArray(manager.getInteger())
        for (i in 0 until bytes.size) {
            bytes[i] = manager.getByte()
        }
    }

    override fun save(manager: SaveManager) {
        super.save(manager)
        manager.putBoolean(toUpdate)
        manager.putBoolean(toUpdate2)
        manager.putInteger(index)
        manager.putInteger(bytes.size)
        for (byte in bytes) {
            manager.putByte(byte)
        }
    }
}