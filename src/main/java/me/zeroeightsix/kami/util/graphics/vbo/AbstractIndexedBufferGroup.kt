package me.zeroeightsix.kami.util.graphics.vbo

import me.zeroeightsix.kami.util.graphics.compat.glBindBuffer
import me.zeroeightsix.kami.util.graphics.compat.glGenBuffers
import org.lwjgl.opengl.GL11.glDrawElements
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER
import java.nio.Buffer
import java.nio.FloatBuffer

abstract class AbstractIndexedBufferGroup<B: Buffer, T: Number>(
    mode: Int,
    usage: Int,
    buffer: FloatBuffer,
    posBuffer: IPosBuffer?,
    colorBuffer: IColorBuffer?,
    texPosBuffer: ITexPosBuffer?,
    private val type: Int
) : AbstractBufferGroup(mode, usage, buffer, posBuffer, colorBuffer, texPosBuffer) {

    protected abstract val indexBuffer: B

    private val indexBufferID by lazy { glGenBuffers() }
    private var indexSize = 0

    abstract fun uploadIndex(indices: Array<T>)

    abstract fun uploadIndex(indices: Iterable<T>)

    protected fun uploadBuffer() {
        indexBuffer.flip()
        indexSize = indexBuffer.limit()

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferID)
        glBufferData()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)

        indexBuffer.clear()
    }

    protected abstract fun glBufferData()

    final override fun render() {
        glBindBuffer(GL_ARRAY_BUFFER, id)
        posBuffer?.preRender()
        colorBuffer?.preRender()
        texPosBuffer?.preRender()

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferID)

        glDrawElements(mode, indexSize, type, 0)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)

        posBuffer?.postRender()
        colorBuffer?.postRender()
        texPosBuffer?.postRender()
        glBindBuffer(GL_ARRAY_BUFFER, 0)
    }
}