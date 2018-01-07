package me.wieku.circuits.input.event

import me.wieku.circuits.world.ClassicWorld

data class KeyDownEvent(val world: ClassicWorld, val keycode: Int)