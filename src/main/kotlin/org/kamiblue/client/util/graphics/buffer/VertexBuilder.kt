package org.kamiblue.client.util.graphics.buffer

import org.kamiblue.commons.tuples.Triple
import org.kamiblue.commons.tuples.floats.Vec2f
import org.kamiblue.commons.tuples.floats.Vec3f
import org.kamiblue.commons.tuples.ints.Vec4i
import java.nio.ByteBuffer

class VertexBuilder(
    private val buffer: ByteBuffer,
    private val posVertexElement: PosVertexElement?,
    private val colorVertexElement: ColorVertexElement?,
    private val texVertexElement: TexVertexElement?
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
        builderPos?.let { posVertexElement?.pos(buffer, it) }
        builderColor?.let { colorVertexElement?.color(buffer, it) }
        builderUV?.let { texVertexElement?.uv(buffer, it) }
    }
}