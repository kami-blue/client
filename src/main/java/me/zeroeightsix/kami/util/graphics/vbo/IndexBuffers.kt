package me.zeroeightsix.kami.util.graphics.vbo

import me.zeroeightsix.kami.util.graphics.compat.*
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER
import org.lwjgl.opengl.GL15.GL_WRITE_ONLY
import java.nio.ByteBuffer
import kotlin.math.max

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
    override val capacity: Int
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
    override val capacity: Int
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

abstract class AbstractIndexBuffer<T : Number> {

    abstract val usage: Int
    abstract val type: Int
    abstract val indexSize: Int
    abstract val capacity: Int

    protected val buffer: ByteBuffer by lazy { BufferUtils.createByteBuffer(capacity * indexSize) }
    private val bufferID by lazy { genBuffers() }

    var bufferSize = 0; private set
    var renderSize = 0; private set

    abstract fun put(input: T)

    fun put(vararg indices: T) {
        indices.forEach(::put)
    }

    fun put(indices: Iterable<T>) {
        indices.forEach(::put)
    }

    fun upload() {
        buffer.flip()
        bindBuffer()
        val newSize = buffer.limit()

        if (newSize > bufferSize || bufferSize - newSize > 128) {
            bufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, usage)
            bufferSize = newSize
        } else {
            bufferSubData(GL_ELEMENT_ARRAY_BUFFER, buffer)
        }

        renderSize = newSize
        unbindBuffer()
        buffer.clear()
    }

    fun render(mode: Int, start: Int, size: Int) {
        bindBuffer()
        glDrawElements(mode, size, type, (start * indexSize).toLong())
        unbindBuffer()
    }

    fun bindBuffer() {
        bindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferID)
    }

    fun unbindBuffer() {
        bindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
    }
}