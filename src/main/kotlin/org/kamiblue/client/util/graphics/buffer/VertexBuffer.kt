package org.kamiblue.client.util.graphics.buffer

import org.kamiblue.commons.tuples.floats.Vec3f
import org.kamiblue.commons.tuples.z
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.glDrawArrays
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER
import java.nio.ByteBuffer

class VertexBuffer private constructor(
    override val usage: Int,
    override val buffer: ByteBuffer,
    private val posVertexElement: PosVertexElement?,
    private val colorVertexElement: ColorVertexElement?,
    private val texVertexElement: TexVertexElement?
) : AbstractBuffer() {

    override val target: Int get() = GL_ARRAY_BUFFER

    private var size = 0

    fun put(block: VertexBuilder.() -> Unit) {
        VertexBuilder(buffer, posVertexElement, colorVertexElement, texVertexElement).apply(block).build()
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
        posVertexElement?.preRender()
        colorVertexElement?.preRender()
        texVertexElement?.preRender()
    }

    fun postRender() {
        posVertexElement?.postRender()
        colorVertexElement?.postRender()
        texVertexElement?.postRender()
        unbindBuffer()
    }

    class Builder(private val usage: Int, private val capacity: Int = 0x10000) {

        private var posVertexElement: PosVertexElement? = null
        private var colorVertexElement: ColorVertexElement? = null
        private var texVertexElement: TexVertexElement? = null

        fun pos2f() {
            posVertexElement = object : PosVertexElement() {
                override val vertexSize: Int
                    get() = 2
            }
        }

        fun pos3f() {
            posVertexElement = object : PosVertexElement() {
                override val vertexSize: Int
                    get() = 3

                override fun pos(buffer: ByteBuffer, pos: Vec3f) {
                    super.pos(buffer, pos)
                    buffer.putFloat(pos.z)
                }
            }
        }

        fun color4b() {
            colorVertexElement = object : ColorVertexElement() {}
        }

        fun tex2f() {
            texVertexElement = object : TexVertexElement() {}
        }

        fun build(): VertexBuffer {
            val buffer = prebuild()
            return VertexBuffer(usage, buffer, posVertexElement, colorVertexElement, texVertexElement)
        }

        private fun prebuild(): ByteBuffer {
            val stride = calcStride()
            var offset = 0L

            posVertexElement?.stride = stride
            colorVertexElement?.stride = stride
            texVertexElement?.stride = stride

            posVertexElement?.offset = offset
            offset += posVertexElement?.byteSize ?: 0

            colorVertexElement?.offset = offset
            offset += colorVertexElement?.byteSize ?: 0

            texVertexElement?.offset = offset

            return BufferUtils.createByteBuffer(capacity * stride)
        }

        private fun calcStride(): Int {
            var stride = 0

            stride += posVertexElement?.byteSize
            stride += colorVertexElement?.byteSize
            stride += texVertexElement?.byteSize

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