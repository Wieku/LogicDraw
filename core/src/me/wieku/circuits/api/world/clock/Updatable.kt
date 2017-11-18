package me.wieku.circuits.api.world.clock

interface Updatable<in T:Number> {

	interface ByTick:Updatable<Long>
	interface ByDelta:Updatable<Float>

	fun update(value: T)
}