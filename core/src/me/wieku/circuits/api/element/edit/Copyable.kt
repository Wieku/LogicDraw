package me.wieku.circuits.api.element.edit

interface Copyable {

	fun copyData(): HashMap<String, Any>

	fun pasteData(data: java.util.HashMap<String, Any>)

}