package me.zeroeightsix.kami.util.graphics.vbo

import me.zeroeightsix.kami.util.graphics.compat.glBindBuffer
import me.zeroeightsix.kami.util.graphics.compat.glBufferData
import me.zeroeightsix.kami.util.graphics.compat.glGenBuffers
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER
import java.nio.FloatBuffer

class BufferGroup private constructor(
    usage: Int,
    buffer: FloatBuffer,
    posBuffer: IPosBuffer?,
    colorBuffer: IColorBuffer?,
    texPosBuffer: ITexPosBuffer?
) : AbstractBufferGroup(usage, buffer, posBuffer, colorBuffer, texPosBuffer) {

    override fun render(mode: Int) {
        render(mode, 0, renderSize)
    }

    override fun render(mode: Int, start: Int, size: Int) {
        glBindBuffer(GL_ARRAY_BUFFER, id)
        posBuffer?.preRender()
        colorBuffer?.preRender()
        texPosBuffer?.preRender()

        glDrawArrays(mode, start, size)

        posBuffer?.postRender()
        colorBuffer?.postRender()
        texPosBuffer?.postRender()
        glBindBuffer(GL_ARRAY_BUFFER, 0)
    }

    class Builder(
        usage: Int,
        capacity: Int = 0x10000
    ) : AbstractBufferGroup.AbstractBuilder<BufferGroup>(usage, capacity) {
        override fun build(): BufferGroup {
            prebuild()
            return BufferGroup(usage, buffer, posBuffer, colorBuffer, texPosBuffer)
        }
    }
}

fun newBufferGroup(
    usage: Int,
    capacity: Int = 0x10000,
    block: BufferGroup.Builder.() -> Unit
) = BufferGroup.Builder(usage, capacity).apply(block).build()