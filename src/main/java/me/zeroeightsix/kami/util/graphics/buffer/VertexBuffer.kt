package me.zeroeightsix.kami.util.graphics.buffer

import org.kamiblue.commons.tuples.floats.Vec3f
import org.kamiblue.commons.tuples.z
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.glDrawArrays
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER
import java.nio.ByteBuffer

class VertexBuffer private constructor(
    override val usage: Int,
    override val buffer: ByteBuffer,
    private val posBuffer: PosVertexElement?,
    private val colorBuffer: ColorVertexElement?,
    private val texPosBuffer: TexVertexElement?
) : AbstractBuffer() {

    override val target: Int get() = GL_ARRAY_BUFFER

    private var size = 0

    fun put(block: VertexBuilder.() -> Unit) {
        VertexBuilder(buffer, posBuffer, colorBuffer, texPosBuffer).apply(block).build()
        size++
    }

    fun upload() {
        upload(size)
        size = 0
    }

    fun render(mode: Int) {
        render(mode, 0, renderSize)
    }

    fun render(mode: Int, start: Int, size: Int) {
        preRender()
        glDrawArrays(mode, start, size)
        postRender()
    }

    fun render(indexBuffer: AbstractIndexBuffer<*>, mode: Int) {
        render(indexBuffer, mode, 0, indexBuffer.renderSize)
    }

    fun render(indexBuffer: AbstractIndexBuffer<*>, mode: Int, start: Int, size: Int) {
        preRender()
        indexBuffer.render(mode, start, size)
        postRender()
    }

    fun preRender() {
        bindBuffer()
        posBuffer?.preRender()
        colorBuffer?.preRender()
        texPosBuffer?.preRender()
    }

    fun postRender() {
        posBuffer?.postRender()
        colorBuffer?.postRender()
        texPosBuffer?.postRender()
        unbindBuffer()
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

        fun build(): VertexBuffer {
            val buffer = prebuild()
            return VertexBuffer(usage, buffer, posBuffer, colorBuffer, texPosBuffer)
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

fun newVertexBuffer(
    usage: Int,
    capacity: Int = 0x10000,
    block: VertexBuffer.Builder.() -> Unit
) = VertexBuffer.Builder(usage, capacity).apply(block).build()