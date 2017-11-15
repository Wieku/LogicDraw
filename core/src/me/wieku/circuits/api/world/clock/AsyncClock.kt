package me.wieku.circuits.api.world.clock

class AsyncClock(private val updatable: Updatable<Number>, tickRate: Int) {
	private var sleepTime: Long = 0
	private var tickRate: Int = tickRate
	set (value) {
		if(tickRate<0) IllegalStateException("Tick Rate has to be positive")
		sleepTime = 1000000000L/value
	}

	var thread: Thread? = null
	var currentTPS: Float = 0f
	var delta: Boolean = false
	var running: Boolean = false

	var currentTick: Long = 0

	fun start() {
		prepareThread()
		running = true
		thread!!.start()
	}

	fun stop() {
		if(!running || thread == null || thread?.state == Thread.State.TERMINATED) return
		running = false

		thread!!.join()
	}

	fun getTPS() = currentTPS

	private fun prepareThread() {
		if(thread != null) {
			stop()
		}

		thread = Thread({
			var currentTime = System.nanoTime()
			var checkTime: Long = 0
			var checkTicks: Long = 0
			while(running) {

				var now = System.nanoTime()
				var updateTime = now - currentTime
				currentTime = now

				checkTime+= updateTime
				++checkTicks

				//TODO: More precise real TPS measuring
				if(checkTime>= 1000000000L) {
					currentTPS = checkTicks.toFloat()
					checkTime = 0
					checkTicks = 0
				}

				updatable.update(if(delta) updateTime/1000000000f else currentTick)

				++currentTick

				var sleepTime = (currentTime-System.nanoTime()+sleepTime)
				var sleepTimeMs = sleepTime/1000000L
				if(sleepTime>0)
					Thread.sleep(sleepTimeMs, (sleepTime-sleepTimeMs).toInt())
			}
		})
	}

	init {
		when (updatable) {
			is Updatable.ByTick -> delta = false
			is Updatable.ByDelta -> delta = true
			else -> IllegalStateException("Updatable must be ByTick or ByDelta!")
		}
	}

}