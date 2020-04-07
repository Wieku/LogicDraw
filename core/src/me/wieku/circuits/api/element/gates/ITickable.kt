package me.wieku.circuits.api.element.gates

interface ITickable {
    var isAlreadyMarked: Boolean
    fun markForUpdate()
    fun update(tick: Long)
}