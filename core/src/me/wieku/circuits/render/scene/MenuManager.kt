package me.wieku.circuits.render.scene

import com.badlogic.gdx.scenes.scene2d.ui.Button
import java.lang.reflect.Field
import java.util.*

object MenuManager {

	private data class ButtonHolder(val button: Button, val reverse: Boolean, val field: Field)

	private val dependencies = HashMap<String, ArrayList<ButtonHolder>>()
	private val values = HashMap<String, Boolean>()

	fun <T:Button> addDependent(dependency: String, item: T, reverse: Boolean = false): T {
		if(!dependencies.containsKey(dependency)) {
			dependencies.put(dependency, ArrayList())
			values.put(dependency, true)
		}

		var claz1: Class<in T> = item.javaClass
		while(claz1 != Button::class.java) {
			claz1 = claz1.superclass
		}

		val field = claz1.getDeclaredField("isDisabled")
		field.isAccessible = true

		dependencies[dependency]!!.add(ButtonHolder(item, reverse, field))
		item.isDisabled =  if(reverse) values[dependency]!! else !values[dependency]!!
		return item
	}

	fun updateDependency(dependency: String, value: Boolean) {
		if(!dependencies.containsKey(dependency)) return

		values[dependency] = value

		dependencies[dependency]!!.forEach {
			it.field.setBoolean(it.button, if(it.reverse) value else !value)
		}
	}

}