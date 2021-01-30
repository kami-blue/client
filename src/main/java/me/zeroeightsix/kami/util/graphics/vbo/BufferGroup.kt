package me.zeroeightsix.kami.util.graphics.vbo

import me.zeroeightsix.kami.util.graphics.compat.bindBuffer
import me.zeroeightsix.kami.util.graphics.compat.bufferData
import me.zeroeightsix.kami.util.graphics.compat.bufferSubData
import me.zeroeightsix.kami.util.graphics.compat.genBuffers
import org.kamiblue.commons.tuples.operations.Vec3f
import org.kamiblue.commons.tuples.z
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.glDrawArrays
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER
import java.nio.ByteBuffer

class BufferGroup private constructor(
    private val usage: Int,
    private val buffer: ByteBuffer,
    private val posBuffer: PosVertexElement?,
    private val colorBuffer: ColorVertexElement?,
    private val texPosBuffer: TexVertexElement?
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

        private var posBuffer: PosVertexElement? = null
        private var colorBuffer: ColorVertexElement? = null
        private var texPosBuffer: TexVertexElement? = null

        fun pos2f() {
            posBuffer = object : PosVertexElement() {
                override val vertexSize: Int
                    get() = 2
            }
        }

        fun pos3f() {
            posBuffer = object : PosVertexElement() {
                override val vertexSize: Int
                    get() = 3

                override fun pos(buffer: ByteBuffer, pos: Vec3f) {
                    super.pos(buffer, pos)
                    buffer.putFloat(pos.z)
                }
            }
        }

        fun color4b() {
            colorBuffer = object : ColorVertexElement() {}
        }

        fun tex2f() {
            texPosBuffer = object : TexVertexElement() {}
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
            offset += posBuffer?.byteSize ?: 0

            colorBuffer?.offset = offset
            offset += colorBuffer?.byteSize ?: 0

            texPosBuffer?.offset = offset

            return BufferUtils.createByteBuffer(capacity * stride)
        }

        private fun calcStride(): Int {
            var stride = 0

            stride += posBuffer?.byteSize
            stride += colorBuffer?.byteSize
            stride += texPosBuffer?.byteSize

            return stride
        }

        private infix operator fun Int.plus(other: Int?) =
            if (other != null) this + other
            else this
    }
}

fun newBufferGroup(
    usage: Int,
    capacity: Int = 0x10000,
    block: BufferGroup.Builder.() -> Unit
) = BufferGroup.Builder(usage, capacity).apply(block).build()