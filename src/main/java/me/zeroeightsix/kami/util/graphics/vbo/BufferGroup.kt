package me.zeroeightsix.kami.util.graphics.vbo

import me.zeroeightsix.kami.util.graphics.compat.glBindBuffer
import me.zeroeightsix.kami.util.graphics.compat.glBufferData
import me.zeroeightsix.kami.util.graphics.compat.glGenBuffers
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER
import java.nio.FloatBuffer

class BufferGroup private constructor(
    mode: Int,
    usage: Int,
    buffer: FloatBuffer,
    posBuffer: IPosBuffer?,
    colorBuffer: IColorBuffer?,
    texPosBuffer: ITexPosBuffer?
) : AbstractBufferGroup(mode, usage, buffer, posBuffer, colorBuffer, texPosBuffer) {

    override fun upload() {
        buffer.flip()

        glBindBuffer(GL_ARRAY_BUFFER, id)
        glBufferData(GL_ARRAY_BUFFER, buffer, usage)
        glBindBuffer(GL_ARRAY_BUFFER, 0)

        buffer.clear()
        renderSize = size
        size = 0
    }

    override fun render() {
        glBindBuffer(GL_ARRAY_BUFFER, id)
        posBuffer?.preRender()
        colorBuffer?.preRender()
        texPosBuffer?.preRender()

        glDrawArrays(mode, 0, renderSize)

        posBuffer?.postRender()
        colorBuffer?.postRender()
        texPosBuffer?.postRender()
        glBindBuffer(GL_ARRAY_BUFFER, 0)
    }

    class Builder(
        mode: Int,
        usage: Int,
        capacity: Int = 0x10000
    ) : AbstractBufferGroup.AbstractBuilder<BufferGroup>(mode, usage, capacity) {

        override fun build(): BufferGroup {
            prebuild()
            return BufferGroup(mode, usage, buffer, posBuffer, colorBuffer, texPosBuffer)
        }

    }

}

fun newBufferGroup(
    mode: Int,
    usage: Int,
    capacity: Int = 0x10000,
    block: BufferGroup.Builder.() -> Unit
) = BufferGroup.Builder(mode, usage, capacity).apply(block).build()

class IndexedBufferGroup private constructor(
    mode: Int,
    usage: Int,
    buffer: FloatBuffer,
    posBuffer: IPosBuffer?,
    colorBuffer: IColorBuffer?,
    texPosBuffer: ITexPosBuffer?
) : AbstractBufferGroup(mode, usage, buffer, posBuffer, colorBuffer, texPosBuffer) {

    private val indexBuffer = BufferUtils.createIntBuffer(buffer.capacity())
    private val indexBufferID by lazy { glGenBuffers() }

    private var indexSize = 0

    override fun upload() {
        buffer.flip()

        glBindBuffer(GL_ARRAY_BUFFER, id)
        glBufferData(GL_ARRAY_BUFFER, buffer, usage)
        glBindBuffer(GL_ARRAY_BUFFER, 0)

        buffer.clear()
        renderSize = size
        size = 0
    }

    fun uploadIndex(vararg indices: Int) {
        indexBuffer.put(indices)
        uploadIndex()
    }

    fun uploadIndex(indices: Array<Int>) {
        indices.forEach(indexBuffer::put)
        uploadIndex()
    }

    fun uploadIndex(indices: Iterable<Int>) {
        indices.forEach(indexBuffer::put)
        uploadIndex()
    }

    private fun uploadIndex() {
        indexBuffer.flip()
        indexSize = indexBuffer.limit()

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferID)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, usage)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)

        indexBuffer.clear()
    }

    override fun render() {
        glBindBuffer(GL_ARRAY_BUFFER, id)
        posBuffer?.preRender()
        colorBuffer?.preRender()
        texPosBuffer?.preRender()

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferID)

        glDrawElements(mode, indexSize, GL_UNSIGNED_INT, 0)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)

        posBuffer?.postRender()
        colorBuffer?.postRender()
        texPosBuffer?.postRender()
        glBindBuffer(GL_ARRAY_BUFFER, 0)
    }

    class Builder(
        mode: Int,
        usage: Int,
        capacity: Int = 0x10000
    ) : AbstractBufferGroup.AbstractBuilder<IndexedBufferGroup>(mode, usage, capacity) {

        override fun build(): IndexedBufferGroup {
            prebuild()
            return IndexedBufferGroup(mode, usage, buffer, posBuffer, colorBuffer, texPosBuffer)
        }

    }

}

fun newIndexedBufferGroup(
    mode: Int,
    usage: Int,
    capacity: Int = 0x10000,
    block: IndexedBufferGroup.Builder.() -> Unit
) = IndexedBufferGroup.Builder(mode, usage, capacity).apply(block).build()