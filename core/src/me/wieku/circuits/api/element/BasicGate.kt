package me.wieku.circuits.api.element

import me.wieku.circuits.api.element.gates.ITickable
import me.wieku.circuits.api.element.gates.ITickableAlways
import me.wieku.circuits.api.element.holders.Inputs
import me.wieku.circuits.api.element.holders.Outputs
import me.wieku.circuits.api.element.input.IController
import me.wieku.circuits.api.element.input.IInput
import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.world.IWorld

abstract class BasicGate(pos: Vector2i) : BasicElement(pos), ITickable {

    protected val inputsAll = Inputs()
    protected val inputs = Inputs()
    protected val controllers = Inputs()

    protected val outputs = Outputs()

    override var isAlreadyMarked: Boolean = false

    protected var world: IWorld? = null

    override fun markForUpdate() {
        if (!isAlreadyMarked && this !is ITickableAlways) {
            world?.markForUpdate(this)
        }
    }

    override fun onPlace(world: IWorld) {
        this.world = world
        updateIO(world)
        world.updateNeighboursOf(pos)
    }

    override fun onRemove(world: IWorld) {
        inputsAll.clear()
        inputs.clear()
        controllers.clear()
        outputs.clear()

        world.updateNeighboursOf(pos)
    }

    override fun onNeighbourChange(position: Vector2i, world: IWorld) {
        updateIO(world)
    }

    protected open fun setOut(value: Boolean) {
        outputs.setActive(value)
    }

    protected open fun updateIO(world: IWorld) {
        inputsAll.clear()
        inputs.clear()
        controllers.clear()
        outputs.clear()

        world.getNeighboursOf(this) {
            when (it) {
                is IInput -> {
                    inputsAll += it
                    if (it is IController) {
                        controllers += it
                    } else {
                        inputs += it
                    }
                }
                is BasicWire -> {
                    val state = it.getState(Axis.getAxis(getPosition(), it.getPosition()))
                    if (state != null)
                        outputs += state
                }
            }
        }

        markForUpdate()
    }

}