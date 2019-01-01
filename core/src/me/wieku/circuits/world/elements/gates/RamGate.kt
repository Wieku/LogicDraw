package me.wieku.circuits.world.elements.gates

import me.wieku.circuits.api.element.BasicInput
import me.wieku.circuits.api.element.BasicWire
import me.wieku.circuits.api.element.edit.Editable
import me.wieku.circuits.api.element.holders.Inputs
import me.wieku.circuits.api.math.Axis
import me.wieku.circuits.api.math.Vector2i
import me.wieku.circuits.api.world.IWorld
import me.wieku.circuits.save.SaveManager
import me.wieku.circuits.world.ClassicWorld
import me.wieku.circuits.world.elements.io.Controller

// spec: https://gist.github.com/magik6k/d1a739a5f032e93aba2742b9fa243a26
class RamGate(pos: Vector2i): SaveableGate(pos), Editable {
	enum class State {
		IDLE, READ, RESPOND
	}

	private val segmentSize = 32 // bytes

	@Editable.Hex("Ram")
	private var memory: ByteArray = ByteArray(1)
		set(value) {
			field = value
			// TODO: round size, maybe
		}


	protected val controllers = Inputs()

	private var chunkSize = 1 // bytes

	private var baseAddr = 0
	private var action = State.IDLE
	private var cmdBuf: Long = 0L
	private var read = 0

	override fun update(tick: Long) {
		val ctl = controllers.isActive()
		val ins = inputs.isActive()

		if(action == State.IDLE) {
			if(ctl) {
				action = State.READ
				cmdBuf = if (ins) {1} else {0}
				read = 0
			}
			state!!.setActiveU(false)
		} else if(action == State.READ) {
			read++
			cmdBuf += if (ins) {(1L).shl(read)} else {0}
			if(!ctl) {
				action = State.RESPOND
			}
			state!!.setActiveU(false)
		} else if(action == State.RESPOND) {
			//println("RESPOND: R:$read B:${java.lang.Long.toHexString(cmdBuf)}")
			if (read > 0) {
				read = 0

				when {
					cmdBuf.and(0x1) == 0L -> { // read
						state!!.setActiveU(read())
					}
					cmdBuf.and(0x03) == 0x01L -> { // write
						state!!.setActiveU(write())
					}
					cmdBuf.and(0x0f) == 0x03L -> { // set segment
						state!!.setActiveU(setSegment())
					}
					cmdBuf.and(0x1f) == 0x0bL -> { // set memory len
						state!!.setActiveU(setMemoryLen())
					}
					cmdBuf.and(0x1f) == 0x1bL -> { // set chunk size
						state!!.setActiveU(setChunkSize())
					}
				}
			} else if(read == 0) {
				// shouldn't happen
				action = State.IDLE
			} else {
				state!!.setActiveU(cmdBuf.and(0x1) == 1L)
				cmdBuf = cmdBuf.ushr(1)
				read += 1
				if(read == 0) {
					action = State.IDLE
				}
			}


		}

		setOut(state!!.isActiveD())
	}

	private fun read(): Boolean {
		val subAddr = cmdBuf.ushr(1).and(0x1f)
		val addr = baseAddr + subAddr
		if(addr >= memory.size) {
			return false
		}
		for(i in 1..chunkSize) {
			val a = i + addr - 1
			if(a >= memory.size) {
				break
			}
			//println("rb:${Integer.toHexString(memory[a.toInt()].toInt().and(0xff))}")
			cmdBuf = cmdBuf.shl(8) + memory[a.toInt()].toInt().and(0xff)
			read -= 8
		}
		return true
	}

	private fun setMemoryLen(): Boolean {
		val mLen = cmdBuf.ushr(5).and(0x0f)
		val newlen = (1).shl(mLen.toInt()) * segmentSize
		memory = memory.copyOf(newlen)
		return true
	}

	private fun setChunkSize(): Boolean {
		val csz = cmdBuf.ushr(5).and(0x03)
		if(csz == 0x03L) {
			return false
		}
		chunkSize = (1).shl(csz.toInt())
		return true
	}

	private fun setSegment(): Boolean {
		val segs = (memory.size + memory.size % segmentSize) / segmentSize - 1
		val abits = Integer.SIZE - Integer.numberOfLeadingZeros(segs)
		if(abits == 0) {
			return true
		}

		val seg = cmdBuf.ushr(4).and(((1).shl(abits) - 1).toLong())

		baseAddr = (seg * segmentSize).toInt()
		return true
	}

	private fun write(): Boolean {
		val subAddr = cmdBuf.ushr(2).and(0x1f)
		val addr = baseAddr + subAddr
		if(addr >= memory.size) {
			return false
		}
		val data = cmdBuf.ushr(2 + 5).and((1L).shl(chunkSize * 8) - 1L)

		for(i in 1..chunkSize) {
			val a = i + addr - 1
			if(a >= memory.size) {
				break
			}
			val b = data.ushr((i - 1)*8).and(0xff).toByte()
			//println("WR: A:${java.lang.Long.toHexString(a)} D:${java.lang.Integer.toHexString(b.toInt())}")
			memory[a.toInt()] = b
		}
		return true
	}

	override fun getIdleColor(): Int = 0xc7b365

	override fun getActiveColor(): Int = 0xc7b365

	override fun updateIO(world: IWorld) {
		inputs.clear()
		outputs.clear()
		controllers.clear()

		world.getNeighboursOf(this) {
			when (it) {
				is BasicInput -> {
					if (it is Controller) controllers += it else inputs += it
				}
				is BasicWire -> {
					outputs += it.getState(Axis.getAxis(getPosition(), it.getPosition()))!!
				}
			}
		}
	}

	override fun load(world: ClassicWorld, manager: SaveManager) {
		super.load(world, manager)

		chunkSize = manager.getInteger()
		baseAddr = manager.getInteger()
		action = State.valueOf(manager.getString())
		cmdBuf = manager.getLong()
		read = manager.getInteger()

		memory = ByteArray(manager.getInteger())
		for (i in 0 until memory.size) {
			memory[i] = manager.getByte()
		}
	}

	override fun save(manager: SaveManager) {
		super.save(manager)

		manager.putInteger(chunkSize)
		manager.putInteger(baseAddr)
		manager.putString(action.name)
		manager.putLong(cmdBuf)
		manager.putInteger(read)

		manager.putInteger(memory.size)
		for (byte in memory) {
			manager.putByte(byte)
		}
	}

}