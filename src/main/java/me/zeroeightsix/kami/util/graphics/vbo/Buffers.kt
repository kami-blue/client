package me.zeroeightsix.kami.util.graphics.vbo

import org.kamiblue.commons.tuples.operations.Vec3f
import org.kamiblue.commons.tuples.z
import java.nio.ByteBuffer

abstract class AbstractBuffer(
    final override val buffer: ByteBuffer
) : IBuffer {
    override var stride: Int = 0
    override var offset: Long = 0L
}

class Pos2Buffer(buffer: ByteBuffer) : AbstractBuffer(buffer), IPosBuffer {
    override val vertexSize: Int = 2
}

class Pos3Buffer(buffer: ByteBuffer) : AbstractBuffer(buffer), IPosBuffer {
    override val vertexSize: Int = 3

    override fun pos(pos: Vec3f) {
        super.pos(pos)
        buffer.putFloat(pos.z)
    }
}

class Color4Buffer(buffer: ByteBuffer) : AbstractBuffer(buffer), IColorBuffer

class TexPosBuffer(buffer: ByteBuffer) : AbstractBuffer(buffer), ITexPosBuffer