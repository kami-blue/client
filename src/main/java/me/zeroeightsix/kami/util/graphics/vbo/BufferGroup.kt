package me.zeroeightsix.kami.util.graphics.vbo

import me.zeroeightsix.kami.util.graphics.compat.GL_ARRAY_BUFFER
import me.zeroeightsix.kami.util.graphics.compat.glBindBuffer
import me.zeroeightsix.kami.util.graphics.compat.glBufferData
import me.zeroeightsix.kami.util.graphics.compat.glGenBuffers
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.glDrawArrays
import java.nio.FloatBuffer

class BufferGroup private constructor(
    private val mode: Int,
    private val usage: Int,
    private val buffer: FloatBuffer,
    private val posBuffer: IPosBuffer?,
    private val colorBuffer: IColorBuffer?,
    private val texPosBuffer: ITexPosBuffer?
) {
    private val id by lazy { glGenBuffers() }

    var size = 0; private set
    private var renderSize = 0

    fun put(block: VertexBuilder.() -> Unit) {
        VertexBuilder(posBuffer, colorBuffer, texPosBuffer).apply(block).build()
        size++
    }

    fun upload() {
        buffer.flip()
        glBindBuffer(GL_ARRAY_BUFFER, id)
        glBufferData(GL_ARRAY_BUFFER, buffer, usage)
        glBindBuffer(GL_ARRAY_BUFFER, 0)

        buffer.clear()
        renderSize = size
        size = 0
    }

    fun render() {
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
        private val mode: Int,
        private val usage: Int,
        capacity: Int = 0x10000
    ) {
        private val buffer = BufferUtils.createFloatBuffer(capacity)

        private var posBuffer: IPosBuffer? = null
        private var colorBuffer: IColorBuffer? = null
        private var texPosBuffer: ITexPosBuffer? = null

        fun pos2Buffer() {
            posBuffer = Pos2Buffer(mode, usage, buffer)
        }

        fun pos3Buffer() {
            posBuffer = Pos3Buffer(mode, usage, buffer)
        }

        fun color3Buffer() {
            colorBuffer = Color3Buffer(mode, usage, buffer)
        }

        fun color4Buffer() {
            colorBuffer = Color4Buffer(mode, usage, buffer)
        }

        fun texPosBuffer() {
            texPosBuffer = TexPosBuffer(mode, usage, buffer)
        }

        fun build(): BufferGroup {
            val stride = calcStride()
            var offset = 0L

            posBuffer?.stride = stride
            colorBuffer?.stride = stride
            texPosBuffer?.stride = stride

            posBuffer?.offset = offset
            offset += posBuffer?.vertexSize * 4

            colorBuffer?.offset = offset
            offset += colorBuffer?.vertexSize * 4

            texPosBuffer?.offset = offset

            return BufferGroup(mode, usage, buffer, posBuffer, colorBuffer, texPosBuffer)
        }

        private fun calcStride(): Int {
            var stride = 0

            stride += posBuffer?.vertexSize
            stride += colorBuffer?.vertexSize
            stride += texPosBuffer?.vertexSize

            return stride * 4
        }

        private infix operator fun Int.plus(other: Int?) =
            if (other != null) this + other
            else this

        private infix operator fun Int?.times(other: Long) =
            if (this != null) this * other
            else 0L
    }
}

fun newBufferGroup(
    mode: Int,
    usage: Int,
    capacity: Int = 0x10000,
    block: BufferGroup.Builder.() -> Unit
) = BufferGroup.Builder(mode, usage, capacity).apply(block).build()