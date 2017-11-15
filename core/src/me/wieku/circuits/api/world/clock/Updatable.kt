package me.wieku.circuits.api.world.clock

interface Updatable<out T:Number> {

	interface ByTick:Updatable<Long>
	interface ByDelta:Updatable<Float>

	fun <T> update(tickNumber: T)
}