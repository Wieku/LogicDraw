package me.wieku.circuits.render.scene

import com.kotcrab.vis.ui.widget.MenuItem
import java.util.*

object MenuManager {

	val dependencies = HashMap<String, ArrayList<MenuItem>>()
	val values = HashMap<String, Boolean>()

	fun addDependent(dependency: String, item: MenuItem) {
		if(!dependencies.containsKey(dependency)) {
			dependencies.put(dependency, ArrayList())
			values.put(dependency, true)
		}

		dependencies[dependency]!!.add(item)
		item.isDisabled = !values[dependency]!!
	}

	fun updateDependency(dependency: String, value: Boolean) {
		if(!dependencies.containsKey(dependency)) return

		values[dependency] = value

		dependencies[dependency]!!.forEach {
			it.isDisabled = !value
		}
	}

}