package me.wieku.circuits.api.collections

import java.util.*

class MappedArray<in K, V>(arraySize: Int) {

	var size = 0
	private set

	val array = Array<Any?>(arraySize) { null }

	val currentElements
	get() = size - indexPool.size

	private val indexes = HashMap<K, Int>()
	private val indexPool = ArrayDeque<Int>()

	fun put(key: K, value: V) {
		when {
			indexes.containsKey(key) -> array[indexes[key]!!] = value
			indexPool.isNotEmpty() -> {
				val index = indexPool.poll()
				indexes.put(key, index)
				array[index] = value
			}
			else -> {
				val index = size++
				indexes.put(key, index)
				array[index] = value
			}
		}
	}

	fun remove(key: K) {
		if(indexes.containsKey(key)) {
			val index = indexes[key]!!
			indexes.remove(key)
			array[index] = null
			indexPool.add(index)
		}
	}

	fun contains(key: K) = indexes.containsKey(key)

	operator fun get(key: K): V? {
		if(indexes.containsKey(key)) {
			return array[indexes[key]!!] as V?
		}
		return null
	}

	operator fun get(index: Int) : V? {
		return array[index] as V?
	}

}