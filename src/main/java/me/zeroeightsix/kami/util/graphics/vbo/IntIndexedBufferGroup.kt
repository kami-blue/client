package me.zeroeightsix.kami.util.graphics.vbo

import me.zeroeightsix.kami.util.graphics.compat.glBufferData
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer

class ByteIndexedBufferGroup private constructor(
    mode: Int,
    usage: Int,
    buffer: FloatBuffer,
    posBuffer: IPosBuffer?,
    colorBuffer: IColorBuffer?,
    texPosBuffer: ITexPosBuffer?
) : AbstractIndexedBufferGroup<ByteBuffer, Byte>(mode, usage, buffer, posBuffer, colorBuffer, texPosBuffer, GL_UNSIGNED_BYTE) {

    override val indexBuffer: ByteBuffer = BufferUtils.createByteBuffer(buffer.capacity())

    fun uploadIndex(vararg indices: Byte) {
        indexBuffer.put(indices)
        uploadBuffer()
    }

    override fun uploadIndex(indices: Array<Byte>) {
        indices.forEach(indexBuffer::put)
        uploadBuffer()
    }

    override fun uploadIndex(indices: Iterable<Byte>) {
        indices.forEach(indexBuffer::put)
        uploadBuffer()
    }

    override fun glBufferData() {
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, usage)
    }

    class Builder(
        mode: Int,
        usage: Int,
        capacity: Int = 0x10000
    ) : AbstractBufferGroup.AbstractBuilder<ByteIndexedBufferGroup>(mode, usage, capacity) {

        override fun build(): ByteIndexedBufferGroup {
            prebuild()
            return ByteIndexedBufferGroup(mode, usage, buffer, posBuffer, colorBuffer, texPosBuffer)
        }

    }

}

fun newByteIndexedBufferGroup(
    mode: Int,
    usage: Int,
    capacity: Int = 0x10000,
    block: ByteIndexedBufferGroup.Builder.() -> Unit
) = ByteIndexedBufferGroup.Builder(mode, usage, capacity).apply(block).build()

class ShortIndexedBufferGroup private constructor(
    mode: Int,
    usage: Int,
    buffer: FloatBuffer,
    posBuffer: IPosBuffer?,
    colorBuffer: IColorBuffer?,
    texPosBuffer: ITexPosBuffer?
) : AbstractIndexedBufferGroup<ShortBuffer, Short>(mode, usage, buffer, posBuffer, colorBuffer, texPosBuffer, GL_UNSIGNED_SHORT) {

    override val indexBuffer: ShortBuffer = BufferUtils.createShortBuffer(buffer.capacity())

    fun uploadIndex(vararg indices: Short) {
        indexBuffer.put(indices)
        uploadBuffer()
    }

    override fun uploadIndex(indices: Array<Short>) {
        indices.forEach(indexBuffer::put)
        uploadBuffer()
    }

    override fun uploadIndex(indices: Iterable<Short>) {
        indices.forEach(indexBuffer::put)
        uploadBuffer()
    }

    override fun glBufferData() {
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, usage)
    }

    class Builder(
        mode: Int,
        usage: Int,
        capacity: Int = 0x10000
    ) : AbstractBufferGroup.AbstractBuilder<ShortIndexedBufferGroup>(mode, usage, capacity) {

        override fun build(): ShortIndexedBufferGroup {
            prebuild()
            return ShortIndexedBufferGroup(mode, usage, buffer, posBuffer, colorBuffer, texPosBuffer)
        }

    }

}

fun newShortIndexedBufferGroup(
    mode: Int,
    usage: Int,
    capacity: Int = 0x10000,
    block: ShortIndexedBufferGroup.Builder.() -> Unit
) = ShortIndexedBufferGroup.Builder(mode, usage, capacity).apply(block).build()

class IntIndexedBufferGroup private constructor(
    mode: Int,
    usage: Int,
    buffer: FloatBuffer,
    posBuffer: IPosBuffer?,
    colorBuffer: IColorBuffer?,
    texPosBuffer: ITexPosBuffer?
) : AbstractIndexedBufferGroup<IntBuffer, Int>(mode, usage, buffer, posBuffer, colorBuffer, texPosBuffer, GL_UNSIGNED_INT) {

    override val indexBuffer: IntBuffer = BufferUtils.createIntBuffer(buffer.capacity())

    fun uploadIndex(vararg indices: Int) {
        indexBuffer.put(indices)
        uploadBuffer()
    }

    override fun uploadIndex(indices: Array<Int>) {
        indices.forEach(indexBuffer::put)
        uploadBuffer()
    }

    override fun uploadIndex(indices: Iterable<Int>) {
        indices.forEach(indexBuffer::put)
        uploadBuffer()
    }

    override fun glBufferData() {
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, usage)
    }

    class Builder(
        mode: Int,
        usage: Int,
        capacity: Int = 0x10000
    ) : AbstractBufferGroup.AbstractBuilder<IntIndexedBufferGroup>(mode, usage, capacity) {

        override fun build(): IntIndexedBufferGroup {
            prebuild()
            return IntIndexedBufferGroup(mode, usage, buffer, posBuffer, colorBuffer, texPosBuffer)
        }

    }

}

fun newIntIndexedBufferGroup(
    mode: Int,
    usage: Int,
    capacity: Int = 0x10000,
    block: IntIndexedBufferGroup.Builder.() -> Unit
) = IntIndexedBufferGroup.Builder(mode, usage, capacity).apply(block).build()