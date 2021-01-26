package me.zeroeightsix.kami.util.graphics.vbo

import me.zeroeightsix.kami.util.graphics.compat.GL_ARRAY_BUFFER
import me.zeroeightsix.kami.util.graphics.compat.glBindBuffer
import me.zeroeightsix.kami.util.graphics.compat.glBufferData
import me.zeroeightsix.kami.util.graphics.compat.glGenBuffers
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.glDrawArrays
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