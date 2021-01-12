package me.zeroeightsix.kami.util.graphics.vbo

import org.lwjgl.opengl.GL11.glDrawArrays

class BufferGroup(
    private val mode: Int,
    private val posBuffer: IPosBuffer?,
    private val colorBuffer: IColorBuffer?,
    private val texPosBuffer: ITexPosBuffer?
) {
    var size = 0; private set
    private var renderSize = 0

    fun put(block: VertexBuilder.() -> Unit) {
        VertexBuilder(posBuffer, colorBuffer, texPosBuffer).apply(block).build()
        size++
    }

    fun upload() {
        posBuffer?.upload()
        colorBuffer?.upload()
        texPosBuffer?.upload()

        posBuffer?.clear()
        colorBuffer?.clear()
        texPosBuffer?.clear()

        renderSize = size
        size = 0
    }

    fun render() {
        posBuffer?.preRender()
        colorBuffer?.preRender()
        texPosBuffer?.preRender()

        glDrawArrays(mode, 0, renderSize)

        posBuffer?.postRender()
        colorBuffer?.postRender()
        texPosBuffer?.postRender()
    }
}