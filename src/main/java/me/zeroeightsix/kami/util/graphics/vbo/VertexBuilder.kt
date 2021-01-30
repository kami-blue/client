package me.zeroeightsix.kami.util.graphics.vbo

import org.kamiblue.commons.tuples.*
import org.kamiblue.commons.tuples.operations.*

class VertexBuilder(
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

    fun pos(posX: Double, posY: Double) {
        builderPos = Vec3f(posX.toFloat(), posY.toFloat(), 0.0f)
    }

    fun pos(posX: Double, posY: Double, posZ: Double) {
        builderPos = Vec3f(posX.toFloat(), posY.toFloat(), posZ.toFloat())
    }

    fun pos(posX: Float, posY: Float) {
        builderPos = Vec3f(posX, posY, 0.0f)
    }

    fun pos(posX: Float, posY: Float, posZ: Float) {
        builderPos = Vec3f(posX, posY, posZ)
    }

    @JvmName("colorI")
    fun color(color: Vec3i) {
        builderColor = Vec4i(color, 255)
    }

    @JvmName("colorI")
    fun color(color: Vec4i) {
        builderColor = color
    }

    fun color(color: Vec3f) {
        builderColor = Vec4i((color.r * 255.0f).toInt(), (color.g * 255.0f).toInt(), (color.b * 255.0f).toInt(), 255)
    }

    fun color(color: Vec4f) {
        builderColor = Vec4i((color.r * 255.0f).toInt(), (color.g * 255.0f).toInt(), (color.b * 255.0f).toInt(), (color.a * 255.0f).toInt())
    }

    fun color(r: Int, g: Int, b: Int) {
        builderColor = Vec4i(r, g, b, 255)
    }

    fun color(r: Int, g: Int, b: Int, a: Int) {
        builderColor = Vec4i(r, g, b, a)
    }

    fun color(r: Float, g: Float, b: Float) {
        builderColor = Vec4i((r * 255.0f).toInt(), (g * 255.0f).toInt(), (b * 255.0f).toInt(), 255)
    }

    fun color(r: Float, g: Float, b: Float, a: Float) {
        builderColor = Vec4i((r * 255.0f).toInt(), (g * 255.0f).toInt(), (b * 255.0f).toInt(), (a * 255.0f).toInt())
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