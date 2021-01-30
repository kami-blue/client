package me.zeroeightsix.kami.util.graphics.vbo

import me.zeroeightsix.kami.util.graphics.compat.*
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.glDrawArrays
import org.lwjgl.opengl.GL15.*
import java.nio.ByteBuffer

class BufferGroup private constructor(
    private val usage: Int,
    private val buffer: ByteBuffer,
    private val posBuffer: IPosBuffer?,
    private val colorBuffer: IColorBuffer?,
    private val texPosBuffer: ITexPosBuffer?
) {

    private val id by lazy { genBuffers() }
    private var size = 0

    var renderSize = 0; private set
    var bufferSize = 0; private set

    fun put(block: VertexBuilder.() -> Unit) {
        VertexBuilder(buffer, posBuffer, colorBuffer, texPosBuffer).apply(block).build()
        size++
    }

    fun upload() {
        bindBuffer()
        buffer.flip()
        val newSize = buffer.limit()

        if (newSize > bufferSize || bufferSize - newSize > 128) {
            bufferData(GL_ARRAY_BUFFER, buffer, usage)
            bufferSize = newSize
        } else {
            bufferSubData(GL_ARRAY_BUFFER, 0L, buffer)
        }

        renderSize = size
        size = 0

        unbindBuffer()
        buffer.clear()
    }

    fun render(mode: Int) {
        render(mode, 0, renderSize)
    }

    fun render(mode: Int, start: Int, size: Int) {
        bindBuffer()
        posBuffer?.preRender()
        colorBuffer?.preRender()
        texPosBuffer?.preRender()

        glDrawArrays(mode, start, size)

        posBuffer?.postRender()
        colorBuffer?.postRender()
        texPosBuffer?.postRender()
        unbindBuffer()
    }

    fun render(indexBuffer: AbstractIndexBuffer<*>, mode: Int) {
        render(indexBuffer, mode, 0, indexBuffer.renderSize)
    }

    fun render(indexBuffer: AbstractIndexBuffer<*>, mode: Int, start: Int, size: Int) {
        bindBuffer()
        posBuffer?.preRender()
        colorBuffer?.preRender()
        texPosBuffer?.preRender()

        indexBuffer.render(mode, start, size)

        posBuffer?.postRender()
        colorBuffer?.postRender()
        texPosBuffer?.postRender()
        unbindBuffer()
    }

    fun bindBuffer() {
        bindBuffer(GL_ARRAY_BUFFER, id)
    }

    fun unbindBuffer() {
        bindBuffer(GL_ARRAY_BUFFER, 0)
    }

    class Builder(private val usage: Int, private val capacity: Int = 0x10000) {

        private var posBuffer: IPosBuffer? = null
        private var colorBuffer: IColorBuffer? = null
        private var texPosBuffer: ITexPosBuffer? = null

        fun pos2Buffer() {
            posBuffer = Pos2Buffer()
        }

        fun pos3Buffer() {
            posBuffer = Pos3Buffer()
        }

        fun color4Buffer() {
            colorBuffer = Color4Buffer()
        }

        fun texPosBuffer() {
            texPosBuffer = TexPosBuffer()
        }

        fun build(): BufferGroup {
            val buffer = prebuild()
            return BufferGroup(usage, buffer, posBuffer, colorBuffer, texPosBuffer)
        }

        private fun prebuild() : ByteBuffer {
            val stride = calcStride()
            var offset = 0L

            posBuffer?.stride = stride
            colorBuffer?.stride = stride
            texPosBuffer?.stride = stride

            posBuffer?.offset = offset
            offset += posBuffer?.vertexSize * 4

            colorBuffer?.offset = offset
            offset += colorBuffer?.vertexSize ?: 0

            texPosBuffer?.offset = offset

            return BufferUtils.createByteBuffer(capacity * stride)
        }

        private fun calcStride(): Int {
            var stride = 0

            stride += posBuffer?.vertexSize * 4
            stride += colorBuffer?.vertexSize
            stride += texPosBuffer?.vertexSize * 4

            return stride
        }

        private infix operator fun Int.plus(other: Int?) =
            if (other != null) this + other
            else this

        private infix operator fun Int?.times(other: Int) =
            if (this != null) this * other
            else 0
    }
}

fun newBufferGroup(
    usage: Int,
    capacity: Int = 0x10000,
    block: BufferGroup.Builder.() -> Unit
) = BufferGroup.Builder(usage, capacity).apply(block).build()