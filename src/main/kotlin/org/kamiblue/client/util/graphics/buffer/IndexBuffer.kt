package me.zeroeightsix.kami.util.graphics.buffer

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER
import java.nio.ByteBuffer

class ByteIndexBuffer(
    override val usage: Int,
    override val capacity: Int = 0x10000
) : AbstractIndexBuffer<Byte>() {

    override val type: Int = GL_UNSIGNED_BYTE
    override val indexSize = 1

    override fun put(input: Byte) {
        buffer.put(input)
    }

    fun put(indices: ByteArray) {
        buffer.put(indices)
        upload()
    }
}

class ShortIndexBuffer(
    override val usage: Int,
    override val capacity: Int = 0x10000
) : AbstractIndexBuffer<Short>() {

    override val type: Int = GL_UNSIGNED_SHORT
    override val indexSize = 2

    override fun put(input: Short) {
        buffer.putShort(input)
    }

    fun put(indices: ShortArray) {
        indices.forEach(::put)
        upload()
    }
}

class IntIndexBuffer(
    override val usage: Int,
    override val capacity: Int = 0x10000
) : AbstractIndexBuffer<Int>() {

    override val type: Int = GL_UNSIGNED_INT
    override val indexSize = 4

    override fun put(input: Int) {
        buffer.putInt(input)
    }

    fun put(indices: IntArray) {
        indices.forEach(::put)
        upload()
    }
}

abstract class AbstractIndexBuffer<T : Number> : AbstractBuffer() {

    abstract val type: Int
    abstract val indexSize: Int
    abstract val capacity: Int

    override val buffer: ByteBuffer by lazy { BufferUtils.createByteBuffer(capacity * indexSize) }
    override val target: Int get() = GL_ELEMENT_ARRAY_BUFFER

    abstract fun put(input: T)

    fun put(vararg indices: T) {
        indices.forEach(::put)
    }

    fun put(indices: Iterable<T>) {
        indices.forEach(::put)
    }

    fun upload() {
        upload(buffer.position())
    }

    fun render(mode: Int, start: Int, size: Int) {
        bindBuffer()
        glDrawElements(mode, size, type, (start * indexSize).toLong())
        unbindBuffer()
    }
}