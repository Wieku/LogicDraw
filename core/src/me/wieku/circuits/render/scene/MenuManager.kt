package me.wieku.circuits.render.scene

import com.badlogic.gdx.scenes.scene2d.ui.Button
import java.util.*

object MenuManager {

	private data class ButtonHolder(val button: Button, val reverse: Boolean)

	private val dependencies = HashMap<String, ArrayList<ButtonHolder>>()
	private val values = HashMap<String, Boolean>()

	fun <T:Button> addDependent(dependency: String, item: T, reverse: Boolean = false): T {
		if(!dependencies.containsKey(dependency)) {
			dependencies.put(dependency, ArrayList())
			values.put(dependency, true)
		}

		dependencies[dependency]!!.add(ButtonHolder(item, reverse))
		item.isDisabled =  if(reverse) values[dependency]!! else !values[dependency]!!
		return item
	}

	fun updateDependency(dependency: String, value: Boolean) {
		if(!dependencies.containsKey(dependency)) return

		values[dependency] = value

		dependencies[dependency]!!.forEach {
			it.button.isDisabled = if(it.reverse) value else !value
		}
	}

}