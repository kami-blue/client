package me.zeroeightsix.kami.util.graphics.vbo

import me.zeroeightsix.kami.util.graphics.compat.glGenBuffers
import org.lwjgl.BufferUtils
import java.nio.FloatBuffer

abstract class AbstractBufferGroup(
    protected val mode: Int,
    protected val usage: Int,
    protected val buffer: FloatBuffer,
    protected val posBuffer: IPosBuffer?,
    protected val colorBuffer: IColorBuffer?,
    protected val texPosBuffer: ITexPosBuffer?
) {

    var size = 0; protected set

    protected val id by lazy { glGenBuffers() }
    protected var renderSize = 0

    fun put(block: VertexBuilder.() -> Unit) {
        VertexBuilder(posBuffer, colorBuffer, texPosBuffer).apply(block).build()
        size++
    }

    abstract fun upload()

    abstract fun render()

    abstract class AbstractBuilder<T : AbstractBufferGroup>(
        protected val mode: Int,
        protected val usage: Int,
        capacity: Int
    ) {
        protected val buffer: FloatBuffer = BufferUtils.createFloatBuffer(capacity)

        protected var posBuffer: IPosBuffer? = null
        protected var colorBuffer: IColorBuffer? = null
        protected var texPosBuffer: ITexPosBuffer? = null

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

        abstract fun build(): T

        protected fun prebuild() {
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