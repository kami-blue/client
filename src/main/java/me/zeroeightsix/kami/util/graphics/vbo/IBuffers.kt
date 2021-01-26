package me.zeroeightsix.kami.util.graphics.vbo

import org.kamiblue.commons.tuples.*
import org.kamiblue.commons.tuples.operations.Vec2f
import org.kamiblue.commons.tuples.operations.Vec3f
import org.kamiblue.commons.tuples.operations.Vec4f
import org.lwjgl.opengl.GL11.*
import java.nio.FloatBuffer

interface IBuffer {
    val mode: Int
    val usage: Int
    val buffer: FloatBuffer

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
        super.preRender()
        glVertexPointer(vertexSize, GL_FLOAT, stride, offset)
    }

    fun pos(pos: Vec3f) {
        buffer.put(pos.x)
        buffer.put(pos.y)
    }
}

interface IColorBuffer : IBuffer {
    override val dataType: Int
        get() = GL_COLOR_ARRAY

    override fun preRender() {
        super.preRender()
        glColorPointer(vertexSize, GL_FLOAT, stride, offset)
    }

    fun color(color: Vec4f) {
        buffer.put(color.r)
        buffer.put(color.g)
        buffer.put(color.b)
    }
}

interface ITexPosBuffer : IBuffer {
    override val dataType: Int
        get() = GL_TEXTURE_COORD_ARRAY

    override val vertexSize: Int
        get() = 2

    override fun preRender() {
        super.preRender()
        glTexCoordPointer(vertexSize, GL_FLOAT, stride, offset)
    }

    fun uv(color: Vec2f) {
        buffer.put(color.u)
        buffer.put(color.v)
    }
}