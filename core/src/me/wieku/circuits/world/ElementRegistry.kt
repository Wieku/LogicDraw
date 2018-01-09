package me.wieku.circuits.world

import com.badlogic.gdx.graphics.Color
import me.wieku.circuits.api.element.BasicElement
import me.wieku.circuits.api.element.IElement
import me.wieku.circuits.api.element.edit.Editable
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.world.elements.Description
import me.wieku.circuits.world.elements.gates.*
import me.wieku.circuits.world.elements.input.Controller
import me.wieku.circuits.world.elements.input.Input
import me.wieku.circuits.world.elements.wire.Cross
import me.wieku.circuits.world.elements.wire.DarkCross
import me.wieku.circuits.world.elements.wire.DarkWire
import me.wieku.circuits.world.elements.wire.Wire
import me.wieku.circuits.world.elements.wire.display.GreenPixel
import me.wieku.circuits.world.elements.wire.display.RedPixel
import me.wieku.circuits.world.elements.wire.display.WhitePixel
import java.util.*

object ElementRegistry {
	val classes: LinkedHashMap<String, Class<out IElement>> = LinkedHashMap()
	val names: LinkedHashMap<Class<out IElement>, String> = LinkedHashMap()
	val editors: LinkedHashMap<Class<out IElement>, ArrayList<Field>> = LinkedHashMap()

	val brushes: LinkedHashMap<String, Color> = LinkedHashMap()

	data class Field(val field: java.lang.reflect.Field, val annotation: Annotation)

	fun get(name: String) = classes[name]

	private fun register(name: String, clazz: Class<out IElement>) {
		classes.put(name, clazz)
		names.put(clazz, name)
		brushes.put(name, Color(clazz.getConstructor(Vector2i::class.java).newInstance(Vector2i()).getIdleColor().shl(8) + 0x9f))

		println(name)
		var claz1: Class<out Any?> = clazz
		while(claz1 != Any::class.java) {
			if(Editable::class.java.isAssignableFrom(claz1)) {
				for(field in claz1.declaredFields) {
					for(annotation in field.annotations) {
						when(annotation){
							is Editable.Spinner,
							is Editable.Boolean,
							is Editable.Key,
							is Editable.Hex ->{
								println(annotation)
								if(!editors.containsKey(clazz))
									editors.put(clazz, ArrayList())
								field.isAccessible = true
								editors[clazz]!!.add(Field(field, annotation))
							}
						}
					}
				}
			}
			claz1 = claz1.superclass
		}
		println()
	}

	init {
		register("wire", Wire::class.java)
		register("cross", Cross::class.java)
		register("input", Input::class.java)
		register("controller", Controller::class.java)
		register("memory", MemoryGate::class.java)
		register("tflipflop", TFFGate::class.java)
		register("or", OrGate::class.java)
		register("nor", NorGate::class.java)
		register("and", AndGate::class.java)
		register("nand", NandGate::class.java)
		register("xor", XorGate::class.java)
		register("xnor", XnorGate::class.java)
		register("pixel_white", WhitePixel::class.java)
		register("pixel_green", GreenPixel::class.java)
		register("pixel_red", RedPixel::class.java)
		register("dark_wire", DarkWire::class.java)
		register("dark_cross", DarkCross::class.java)
		register("description", Description::class.java)
		register("delay", DelayGate::class.java)
		register("pwm", PWMGate::class.java)
		register("debug_stop", StopGate::class.java)
		register("programmer", ProgramInputGate::class.java)
		register("key", KeyGate::class.java)
	}
}