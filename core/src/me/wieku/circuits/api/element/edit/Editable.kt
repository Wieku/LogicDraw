package me.wieku.circuits.api.element.edit

interface Editable {

	@Target(AnnotationTarget.FIELD)
	annotation class Spinner(val name: String, val model: IntArray)

}