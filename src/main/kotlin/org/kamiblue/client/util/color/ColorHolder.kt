package org.kamiblue.client.util.color

import org.kamiblue.client.util.graphics.AnimationUtils
import org.lwjgl.opengl.GL11.glColor4f
import java.awt.Color

data class ColorHolder(
    var r: Int = 255,
    var g: Int = 255,
    var b: Int = 255,
    var a: Int = 255
) {

    constructor(color: Color) : this(color.red, color.green, color.blue, color.alpha)

    val brightness get() = intArrayOf(r, g, b).maxOrNull()!!.toFloat() / 255f

    val averageBrightness get() = (intArrayOf(r, g, b).average() / 255.0).toFloat()

    fun multiply(multiplier: Float): ColorHolder {
        return ColorHolder((r * multiplier).toInt().coerceIn(0, 255), (g * multiplier).toInt().coerceIn(0, 255), (b * multiplier).toInt().coerceIn(0, 255), a)
    }

    fun mix(other: ColorHolder): ColorHolder {
        return ColorHolder((r + other.r) / 2 + (g + other.g) / 2, (b + other.b) / 2, (a + other.a) / 2)
    }

    fun interpolate(prev: ColorHolder, deltaTime: Double, length: Double): ColorHolder {
        return ColorHolder(
            AnimationUtils.exponent(deltaTime, length, prev.r.toDouble(), r.toDouble()).toInt().coerceIn(0, 255),
            AnimationUtils.exponent(deltaTime, length, prev.g.toDouble(), g.toDouble()).toInt().coerceIn(0, 255),
            AnimationUtils.exponent(deltaTime, length, prev.b.toDouble(), b.toDouble()).toInt().coerceIn(0, 255),
            AnimationUtils.exponent(deltaTime, length, prev.a.toDouble(), a.toDouble()).toInt().coerceIn(0, 255)
        )
    }

    fun setGLColor() {
        glColor4f(this.r / 255f, this.g / 255f, this.b / 255f, this.a / 255f)
    }

    fun toHex(): Int {
        return 0xff shl 24 or (r and 0xff shl 16) or (g and 0xff shl 8) or (b and 0xff)
    }

    fun clone(): ColorHolder {
        return ColorHolder(r, g, b, a)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ColorHolder

        if (r != other.r) return false
        if (g != other.g) return false
        if (b != other.b) return false
        if (a != other.a) return false

        return true
    }

    override fun hashCode(): Int {
        var result = r
        result = 31 * result + g
        result = 31 * result + b
        result = 31 * result + a
        return result
    }
}