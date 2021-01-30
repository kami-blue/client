package me.zeroeightsix.kami.util.graphics.vbo

import org.kamiblue.commons.tuples.*
import org.kamiblue.commons.tuples.operations.Vec2f
import org.kamiblue.commons.tuples.operations.Vec3f
import org.kamiblue.commons.tuples.operations.Vec4f
import org.kamiblue.commons.tuples.operations.Vec4i
import org.lwjgl.opengl.GL11.*
import java.nio.ByteBuffer
import java.nio.FloatBuffer

interface IBuffer {
    val buffer: ByteBuffer

    val dataType: Int
    val vertexSize: Int

    var stride: Int
    var offset: Long

    fun preRender() {
        glEnableClientState(dataType)
    }

    fun postRender() {
        glDisableClientState(dataType)
    }
}

interface IPosBuffer : IBuffer {
    override val dataType: Int
        get() = GL_VERTEX_ARRAY

    override fun preRender() {
        glVertexPointer(vertexSize, GL_FLOAT, stride, offset)
        super.preRender()
    }

    fun pos(pos: Vec3f) {
        buffer.putFloat(pos.x)
        buffer.putFloat(pos.y)
    }
}

interface IColorBuffer : IBuffer {
    override val dataType: Int
        get() = GL_COLOR_ARRAY

    override val vertexSize: Int
        get() = 4

    override fun preRender() {
        glColorPointer(vertexSize, GL_UNSIGNED_BYTE, stride, offset)
        super.preRender()
    }

    fun color(color: Vec4i) {
        buffer.put((color.r).toByte())
        buffer.put((color.g).toByte())
        buffer.put((color.b).toByte())
        buffer.put((color.a).toByte())
    }
}

interface ITexPosBuffer : IBuffer {
    override val dataType: Int
        get() = GL_TEXTURE_COORD_ARRAY

    override val vertexSize: Int
        get() = 2

    override fun preRender() {
        glTexCoordPointer(vertexSize, GL_FLOAT, stride, offset)
        super.preRender()
    }

    fun uv(color: Vec2f) {
        buffer.putFloat(color.u)
        buffer.putFloat(color.v)
    }
}