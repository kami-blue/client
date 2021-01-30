package me.zeroeightsix.kami.util.graphics.vbo

import org.kamiblue.commons.tuples.operations.Vec3f
import org.kamiblue.commons.tuples.z
import java.nio.ByteBuffer

abstract class AbstractBuffer : IBuffer {
    override var stride: Int = 0
    override var offset: Long = 0L
}

class Pos2Buffer : AbstractBuffer(), IPosBuffer {
    override val vertexSize: Int = 2
}

class Pos3Buffer : AbstractBuffer(), IPosBuffer {
    override val vertexSize: Int = 3

    override fun pos(buffer: ByteBuffer, pos: Vec3f) {
        super.pos(buffer, pos)
        buffer.putFloat(pos.z)
    }
}

class Color4Buffer : AbstractBuffer(), IColorBuffer

class TexPosBuffer : AbstractBuffer(), ITexPosBuffer