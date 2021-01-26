package me.zeroeightsix.kami.util.graphics.vbo

import org.kamiblue.commons.tuples.a
import org.kamiblue.commons.tuples.operations.Vec3f
import org.kamiblue.commons.tuples.operations.Vec4f
import org.kamiblue.commons.tuples.z
import java.nio.FloatBuffer

abstract class AbstractBuffer(
    final override val mode: Int,
    final override val usage: Int,
    final override val buffer: FloatBuffer
) : IBuffer {
    override var stride: Int = 0
    override var offset: Long = 0L
}

class Pos2Buffer(mode: Int, usage: Int, buffer: FloatBuffer) : AbstractBuffer(mode, usage, buffer), IPosBuffer {
    override val vertexSize: Int = 2
}

class Pos3Buffer(mode: Int, usage: Int, buffer: FloatBuffer) : AbstractBuffer(mode, usage, buffer), IPosBuffer {
    override val vertexSize: Int = 3

    override fun pos(pos: Vec3f) {
        super.pos(pos)
        buffer.put(pos.z)
    }
}

class Color3Buffer(mode: Int, usage: Int, buffer: FloatBuffer) : AbstractBuffer(mode, usage, buffer), IColorBuffer {
    override val vertexSize: Int = 3
}

class Color4Buffer(mode: Int, usage: Int, buffer: FloatBuffer) : AbstractBuffer(mode, usage, buffer), IColorBuffer {
    override val vertexSize: Int = 4

    override fun color(color: Vec4f) {
        super.color(color)
        buffer.put(color.a)
    }
}

class TexPosBuffer(mode: Int, usage: Int, buffer: FloatBuffer) : AbstractBuffer(mode, usage, buffer), ITexPosBuffer