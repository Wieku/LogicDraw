package me.wieku.circuits.world.elements.io

import me.wieku.circuits.api.element.BasicElement
import me.wieku.circuits.api.element.BasicWire
import me.wieku.circuits.api.element.edit.Copyable
import me.wieku.circuits.api.element.edit.Editable
import me.wieku.circuits.api.element.gates.ITickable
import me.wieku.circuits.api.element.input.IInput
import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.state.State
import me.wieku.circuits.api.world.IWorld
import me.wieku.circuits.save.SaveManager
import me.wieku.circuits.save.Saveable
import me.wieku.circuits.world.ClassicWorld

open class Input(pos: Vector2i): BasicElement(pos), IInput, Saveable, Editable, Copyable {

	private var state: State? = null

	val inputs = Array<State?>(4) { null }
	val outputs = ArrayList<ITickable>(4)
	var size = 0

	@Editable.Boolean("Inverted signal")
	var inverted = false
	private set

	override fun isActive(): Boolean {
		for(i in 0 until size) {
			/*if (inputs[i] != null) {
				println("state ${inputs[i]?.id} ${inputs[i]?.activeNum}")
			}*/
			if(inputs[i]!!.isActive()) {
				val intSt = !inverted
				state!!.setActiveU(intSt)
				return intSt
			}
		}

		state!!.setActiveU(inverted)

		return inverted
	}

	private fun isActiveF(): Boolean {
		for(i in 0 until size) {
			if (inputs[i] != null && inputs[i]!!.isActive()) {
				return true
			}
		}
		return false
	}

	override fun onPlace(world: IWorld) {
		state = world.getStateManager().createState()
		updateIO(world)
		world.updateNeighboursOf(pos)
	}

	override fun onNeighbourChange(position: Vector2i, world: IWorld) {
		updateIO(world)
	}

	private fun updateIO(world: IWorld) {
		size = 0
		for (i in 0..3) {
			inputs[i]?.deleteInput(this)
		}
		inputs.fill(null)
		outputs.clear()

		world.getNeighboursOf(this) {
			when(it) {
				is BasicWire -> {
					var intSt = it.getState(Axis.getAxis(getPosition(), it.getPosition()))
					if(intSt != null) {
						inputs[size++] = intSt
					}
				}
				is ITickable -> {
					outputs.add(it)
				}
			}
		}

		for (i in 0..3) {
			inputs[i]?.addInput(this)
		}
	}

	override fun getGates(): List<ITickable> = outputs

	override fun onRemove(world: IWorld) {
		state!!.unregister()
		world.updateNeighboursOf(pos)
	}

	override fun getIdleColor(): Int = 0x01579B

	override fun getActiveColor(): Int = 0x0277BD

	override fun getColor(): Int = if(state != null && isActiveF()) getActiveColor() else getIdleColor()

	override fun setState(state: State, axis: Axis) {}

	override fun getState(axis: Axis): State? = state

	override fun save(manager: SaveManager) {
		manager.putInteger(state!!.id)
		if(manager.getVersion() >= 2) {
			manager.putBoolean(inverted)
		}
	}

	override fun load(world: ClassicWorld, manager: SaveManager) {
		state = world.getStateManager().getState(manager.getInteger())!!
		state!!.register()
		if(manager.getVersion() >= 2) {
			inverted = manager.getBoolean()
		}
	}

	override fun afterLoad(world: IWorld) {
		updateIO(world)
	}

	override fun copyData(): HashMap<String, Any> {
		val map = HashMap<String, Any>()
		map.put("state", state!!.isActive())
		map.put("inverted", inverted)
		return map
	}

	override fun pasteData(data: HashMap<String, Any>) {
		val bool = data["state"] as Boolean
		state?.setActive(bool)
		inverted = data["inverted"] as Boolean
	}

}