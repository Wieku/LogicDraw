package me.wieku.circuits.api.world

import me.wieku.circuits.api.element.IElement
import me.wieku.circuits.api.element.gates.ITickable
import me.wieku.circuits.api.state.StateManager
import me.wieku.circuits.api.math.Vector2i

interface IWorld {

	fun update(tick: Long)

	fun placeElement(position:Vector2i, name:String)
	fun removeElement(position: Vector2i)
	fun getElement(position: Vector2i): IElement?
	fun getNeighboursOf(element: IElement): Array<IElement>
	fun getNeighboursOf(element: IElement, consumer: (IElement) -> Unit)
	fun updateNeighboursOf(pos: Vector2i)
	fun markForUpdate(tickable: ITickable)
	fun getStateManager() : StateManager
}