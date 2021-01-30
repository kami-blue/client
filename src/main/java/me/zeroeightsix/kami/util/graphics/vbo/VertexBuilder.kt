package me.zeroeightsix.kami.util.graphics.vbo

import org.kamiblue.commons.tuples.*
import org.kamiblue.commons.tuples.operations.*
import java.nio.ByteBuffer

class VertexBuilder(
    private val buffer: ByteBuffer,
    private val posBuffer: IPosBuffer?,
    private val colorBuffer: IColorBuffer?,
    private val texPosBuffer: ITexPosBuffer?
) {
    private var builderPos: Vec3f? = null
    private var builderColor: Vec4i? = null
    private var builderUV: Vec2f? = null

    fun pos(pos: Vec2f) {
        builderPos = Triple(pos, 0.0f)
    }

    fun pos(pos: Vec3f) {
        builderPos = pos
    }

    fun color(color: Vec4i) {
        builderColor = color
    }

    fun tex(uv: Vec2f) {
        builderUV = uv
    }

    fun build() {
        builderPos?.let { posBuffer?.pos(buffer, it) }
        builderColor?.let { colorBuffer?.color(buffer, it) }
        builderUV?.let { texPosBuffer?.uv(buffer, it) }
    }
}