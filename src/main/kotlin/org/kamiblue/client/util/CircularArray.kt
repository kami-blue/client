package org.kamiblue.client.util

class CircularArray<T : Number> constructor(private val array: Array<T>, filled: Boolean) : Iterable<T> {
    private var _size = if (filled) array.size else 0
    private var tail = -1

    companion object {
        inline fun <reified T : Number> create(size: Int, fill: T) = CircularArray(Array(size) { fill }, true)
        inline fun <reified T : Number> create(size: Int) = CircularArray(Array(size) { 0 as T }, false)
    }

    val head: Int
        get() =
            if (_size == array.size)
                (tail + 1) % array.size
            else
                0

    val size: Int get() = _size

    fun add(item: T) {
        tail = (tail + 1) % array.size
        array[tail] = item
        if (_size < array.size) _size++
    }

    fun reset() {
        this._size = 0
        this.tail = 0
    }

    fun average(): Float {
        var sum = 0.0
        for (i in 0 until size) {
            sum += array[i].toDouble()
        }
        return if (_size == 0) 0.0f else (sum / size).toFloat()
    }

    @Suppress("UNCHECKED_CAST")
    private operator fun get(index: Int): T =
        when {
            _size == 0 || index > _size || index < 0 -> throw IndexOutOfBoundsException("$index")
            _size == array.size -> array[(head + index) % array.size]
            else -> array[index]
        }

    override fun iterator() = object : Iterator<T> {
        private var index = 0
        override fun hasNext(): Boolean = index < size
        override fun next(): T = try { get(index++) } catch (e: IndexOutOfBoundsException) { index -= 1; throw NoSuchElementException(e.message) }
    }
}