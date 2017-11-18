package me.wieku.circuits.api.world.clock

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class AsyncClock (private val updatable: Updatable<*>, tickRate: Int)  {
	private var sleepTime: Long = 1000000000L/tickRate

	private var tickRate: Int = tickRate
	set (value) {
		if(tickRate<0) IllegalStateException("Tick Rate has to be positive")
		sleepTime = 1000000000L/value
	}

	var currentTPS: Float = 0f

	private var task:ScheduledFuture<*>? = null
	private var delta: Boolean = false
	private var currentTick: Long = 0

	@Suppress("UNCHECKED_CAST")
	fun start() {
		if(task != null && !task!!.isCancelled) IllegalStateException("AsyncClock is already running!")

		var lastCheck = System.nanoTime()
		var timeCheck = 0L
		var tempTPS = 0L
		task = executor.scheduleAtFixedRate({

			var updateTime = System.nanoTime() - lastCheck
			lastCheck = System.nanoTime()

			timeCheck+=updateTime

			++tempTPS

			if(timeCheck>=1000000000) {
				currentTPS = tempTPS.toFloat()
				tempTPS = 0
				timeCheck = 0
			}

			(updatable as Updatable<Number>).update(if (delta) updateTime / 1000000000f else currentTick)

			++currentTick

		}, 0, sleepTime, TimeUnit.NANOSECONDS)
	}

	fun stop() {
		task?.cancel(false)
	}

	fun getTPS() = currentTPS

	companion object {
		val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(5)
	}

	init {
		when (updatable) {
			is Updatable.ByTick -> delta = false
			is Updatable.ByDelta -> delta = true
			else -> IllegalStateException("Updatable must be ByTick or ByDelta!")
		}
	}

}