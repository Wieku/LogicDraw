package me.wieku.circuits.api.element.input

import me.wieku.circuits.api.element.gates.ITickable

interface IInput {
    fun isActive(): Boolean
    fun getGates(): List<ITickable>
}