package me.wieku.circuits.input.event

import me.wieku.circuits.world.ClassicWorld

data class KeyUpEvent(val world: ClassicWorld, val keycode: Int)