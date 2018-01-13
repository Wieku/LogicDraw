package me.wieku.circuits.world.elements.gates

import com.badlogic.gdx.Input
import com.google.common.eventbus.Subscribe
import com.sun.org.apache.xpath.internal.operations.Bool
import me.wieku.circuits.api.element.edit.Editable
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.world.IWorld
import me.wieku.circuits.input.event.KeyDownEvent
import me.wieku.circuits.input.event.KeyUpEvent
import me.wieku.circuits.save.SaveManager
import me.wieku.circuits.world.ClassicWorld

class KeyGate(pos: Vector2i): SaveableGate(pos), Editable {

	@Editable.Key("Key")
	var keycode = Input.Keys.L

	@Editable.Boolean("Bistable")
	var bistable = false

	var locked = false

	override fun update(tick: Long) {
		var calc = inputs.isActive()

		if(calc) {
			state!!.setActive(false)
			locked = true
		} else {
			locked = false
		}

		setOut(state!!.isActiveD())
	}

	override fun getIdleColor(): Int = 0x3E2723

	override fun getActiveColor(): Int = 0x4E342E

	@Subscribe fun onKeyDown(event: KeyDownEvent) {
		if(locked) return
		if(event.keycode == keycode) {
			if(bistable) {
				state!!.setActive(!state!!.isActive())
			} else state!!.setActive(true)
		}
	}

	@Subscribe fun onKeyUp(event: KeyUpEvent) {
		if(locked) return
		if(event.keycode == keycode) {
			if(!bistable) state!!.setActive(false)
		}
	}

	override fun save(manager: SaveManager) {
		super.save(manager)
		manager.putInteger(keycode)
		manager.putByte(if(bistable) 1 else 0)
	}

	override fun load(world: ClassicWorld, manager: SaveManager) {
		super.load(world, manager)
		keycode = manager.getInteger()
		bistable = manager.getByte() == (1).toByte()
	}

	override fun onPlace(world: IWorld) {
		super.onPlace(world)
		(world as ClassicWorld).eventBus.register(this)
	}

	override fun onRemove(world: IWorld) {
		super.onRemove(world)
		(world as ClassicWorld).eventBus.unregister(this)
	}

	override fun afterLoad(world: ClassicWorld) {
		super.afterLoad(world)
		(world as ClassicWorld).eventBus.register(this)
	}

	override fun copyData(): HashMap<String, Any> {
		val map =  super.copyData()
		map.put("keycode", keycode)
		map.put("bistable", bistable)
		map.put("locked", locked)
		return map
	}

	override fun pasteData(data: HashMap<String, Any>) {
		super.pasteData(data)
		keycode = data["keycode"] as Int
		bistable = data["bistable"] as Boolean
		locked = data["locked"] as Boolean
	}

}