package me.wieku.circuits.world.elements.gates

import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.save.SaveManager
import me.wieku.circuits.world.ClassicWorld


class SerialIOGate(pos: Vector2i): SaveableGate(pos) {
    enum class State {
        IDLE, CMD, READ, RESPOND
    }

    private var action = State.IDLE
    private var n = 0
    private var b = 0

    override fun update(tick: Long) {
        val ins = inputs.isActive()
        state!!.setActiveU(false)

        if(action == State.IDLE) {
            if(ins) {
                action = State.CMD
            }
        } else if (action == State.CMD) {
            if(ins) { // 1 - write
                action = State.READ
                n = 8
                b = 0
            } else { // 0 - read
                if(System.`in`.available() <= 0) {
                    action = State.IDLE
                    return
                }
                state!!.setActiveU(true)
                action = State.RESPOND
                n = 8
                b = System.`in`.read()
            }
        } else if (action == State.READ) {
            b = b.shl(1) + if(ins) 1 else 0
            n--
            if(n <= 0) {
                action = State.IDLE
                System.out.print(b.toChar())
                System.out.flush()
            }
        } else if (action == State.RESPOND) {
            state!!.setActiveU(b.and(1) == 1)
            b = b.shr(1)
            n--
            if(n <= 0) {
                action = State.IDLE
            }
        }

        setOut(state!!.isActiveD())
    }

    override fun getIdleColor(): Int = 0xc99ffe

    override fun getActiveColor(): Int = 0xc99ffe

    override fun load(world: ClassicWorld, manager: SaveManager) {
        super.load(world, manager)

        n = manager.getInteger()
        b = manager.getInteger()
        action = State.valueOf(manager.getString())
    }

    override fun save(manager: SaveManager) {
        super.save(manager)

        manager.putInteger(n)
        manager.putInteger(b)
        manager.putString(action.name)
    }
}
