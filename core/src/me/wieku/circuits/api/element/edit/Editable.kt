package me.wieku.circuits.api.element.edit

interface Editable {

	@Target(AnnotationTarget.FIELD)
	annotation class Spinner(val name: String, val model: IntArray)

	@Target(AnnotationTarget.FIELD)
	annotation class Boolean(val name: String)

	@Target(AnnotationTarget.FIELD)
	annotation class Key(val name: String)

	@Target(AnnotationTarget.FIELD)
	annotation class Text(val name: String)

	@Target(AnnotationTarget.FIELD)
	annotation class Hex(val name: String)

}