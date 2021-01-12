package me.zeroeightsix.kami.util.graphics.vbo

import org.kamiblue.commons.tuples.Triple
import org.kamiblue.commons.tuples.operations.Vec2f
import org.kamiblue.commons.tuples.operations.Vec3f
import org.kamiblue.commons.tuples.operations.Vec4f

class VertexBuilder(
    private val posBuffer: IPosBuffer?,
    private val colorBuffer: IColorBuffer?,
    private val texPosBuffer: ITexPosBuffer?
) {
    private var builderPos: Vec3f? = null
    private var builderColor: Vec4f? = null
    private var builderUV: Vec2f? = null

    fun pos(pos: Vec2f) {
        builderPos = Triple(pos, 0.0f)
    }

    fun pos(pos: Vec3f) {
        builderPos = pos
    }

    fun pos(posX: Float, posY: Float) {
        builderPos = Vec3f(posX, posY, 0.0f)
    }

    fun pos(posX: Float, posY: Float, posZ: Float) {
        builderPos = Vec3f(posX, posY, posZ)
    }

    fun color(color: Vec3f) {
        builderColor = Vec4f(color, 1.0f)
    }

    fun color(color: Vec4f) {
        builderColor = color
    }

    fun color(r: Float, g: Float, b: Float) {
        builderColor = Vec4f(r, g, b, 1.0f)
    }

    fun color(r: Float, g: Float, b: Float, a: Float) {
        builderColor = Vec4f(r, g, b, a)
    }

    fun tex(uv: Vec2f) {
        builderUV = uv
    }

    fun tex(u: Float, v: Float) {
        builderUV = Vec2f(u, v)
    }

    fun build() {
        builderPos?.let { posBuffer?.pos(it) }
        builderColor?.let { colorBuffer?.color(it) }
        builderUV?.let { texPosBuffer?.uv(it) }
    }
}