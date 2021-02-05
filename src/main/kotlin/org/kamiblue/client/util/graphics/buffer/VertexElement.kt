package me.zeroeightsix.kami.util.graphics.buffer

import org.kamiblue.commons.tuples.*
import org.kamiblue.commons.tuples.floats.Vec2f
import org.kamiblue.commons.tuples.floats.Vec3f
import org.kamiblue.commons.tuples.ints.Vec4i
import org.lwjgl.opengl.GL11.*
import java.nio.ByteBuffer

abstract class AbstractVertexElement {
    protected abstract val dataType: Int
    abstract val vertexSize: Int
    abstract val byteSize: Int

    var stride: Int = 0
    var offset: Long = 0L

    open fun preRender() {
        glEnableClientState(dataType)
    }

    fun postRender() {
        glDisableClientState(dataType)
    }
}

abstract class PosVertexElement : AbstractVertexElement() {
    final override val dataType: Int
        get() = GL_VERTEX_ARRAY

    final override val byteSize: Int
        get() = vertexSize * 4

    final override fun preRender() {
        glVertexPointer(vertexSize, GL_FLOAT, stride, offset)
        super.preRender()
    }

    open fun pos(buffer: ByteBuffer, pos: Vec3f) {
        buffer.putFloat(pos.x)
        buffer.putFloat(pos.y)
    }
}

abstract class ColorVertexElement : AbstractVertexElement() {
    final override val dataType: Int
        get() = GL_COLOR_ARRAY

    final override val vertexSize: Int
        get() = 4

    final override val byteSize: Int
        get() = 4

    final override fun preRender() {
        glColorPointer(vertexSize, GL_UNSIGNED_BYTE, stride, offset)
        super.preRender()
    }

    fun color(buffer: ByteBuffer, color: Vec4i) {
        buffer.put((color.r).toByte())
        buffer.put((color.g).toByte())
        buffer.put((color.b).toByte())
        buffer.put((color.a).toByte())
    }
}

abstract class TexVertexElement : AbstractVertexElement() {
    final override val dataType: Int
        get() = GL_TEXTURE_COORD_ARRAY

    final override val vertexSize: Int
        get() = 2

    final override val byteSize: Int
        get() = 8

    final override fun preRender() {
        glTexCoordPointer(vertexSize, GL_FLOAT, stride, offset)
        super.preRender()
    }

    fun uv(buffer: ByteBuffer, uv: Vec2f) {
        buffer.putFloat(uv.u)
        buffer.putFloat(uv.v)
    }
}