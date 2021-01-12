package me.zeroeightsix.kami.util.graphics.vbo

import org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW

fun newBufferGroup(
    mode: Int,
    usage: Int = GL_DYNAMIC_DRAW,
    capacity: Int = 4096,
    block: BufferGroupBuilder.() -> Unit
) =
    BufferGroupBuilder(mode, usage, capacity).apply(block).build()

class BufferGroupBuilder(
    private val mode: Int,
    private val usage: Int,
    private val capacity: Int = 4096
) {
    private var posBuffer: IPosBuffer? = null
    private var colorBuffer: IColorBuffer? = null
    private var texPosBuffer: ITexPosBuffer? = null

    fun pos2Buffer() {
        posBuffer = Pos2Buffer(mode, usage, capacity)
    }

    fun pos3Buffer() {
        posBuffer = Pos3Buffer(mode, usage, capacity)
    }

    fun color3Buffer() {
        colorBuffer = Color3Buffer(mode, usage, capacity)
    }

    fun color4Buffer() {
        colorBuffer = Color4Buffer(mode, usage, capacity)
    }

    fun texPosBuffer() {
        texPosBuffer = TexPosBuffer(mode, usage, capacity)
    }

    fun build() = BufferGroup(mode, posBuffer, colorBuffer, texPosBuffer)
}