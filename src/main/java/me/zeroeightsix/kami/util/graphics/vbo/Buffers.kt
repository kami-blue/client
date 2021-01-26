package me.zeroeightsix.kami.util.graphics.vbo

import net.minecraft.client.renderer.GLAllocation
import org.kamiblue.commons.tuples.a
import org.kamiblue.commons.tuples.operations.Vec3f
import org.kamiblue.commons.tuples.operations.Vec4f
import org.kamiblue.commons.tuples.z
import org.lwjgl.opengl.GL15.glGenBuffers
import java.nio.FloatBuffer

abstract class AbstractBuffer(
    override val mode: Int,
    override val usage: Int,
    final override val capacity: Int
) : IBuffer {
    override val id: Int = glGenBuffers()
    override val buffer: FloatBuffer = GLAllocation.createDirectFloatBuffer(capacity)
}

class Pos2Buffer(mode: Int, usage: Int, capacity: Int = 0x10000) : AbstractBuffer(mode, usage, capacity), IPosBuffer {
    override val vertexSize: Int = 2
}

class Pos3Buffer(mode: Int, usage: Int, capacity: Int = 0x10000) : AbstractBuffer(mode, usage, capacity), IPosBuffer {
    override val vertexSize: Int = 3

    override fun pos(pos: Vec3f) {
        super.pos(pos)
        buffer.put(pos.z)
    }
}

class Color3Buffer(mode: Int, usage: Int, capacity: Int = 0x10000) : AbstractBuffer(mode, usage, capacity), IColorBuffer {
    override val vertexSize: Int = 3
}

class Color4Buffer(mode: Int, usage: Int, capacity: Int = 0x10000) : AbstractBuffer(mode, usage, capacity), IColorBuffer {
    override val vertexSize: Int = 4

    override fun color(color: Vec4f) {
        super.color(color)
        buffer.put(color.a)
    }
}

class TexPosBuffer(mode: Int, usage: Int, capacity: Int = 0x10000) : AbstractBuffer(mode, usage, capacity), ITexPosBuffer