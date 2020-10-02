package me.zeroeightsix.kami.util.color

import org.lwjgl.opengl.GL11.glColor4f
import java.awt.Color

/**
 * Created by Gebruiker on 18/04/2017.
 * Updated by Xiaro on 09/08/20
 */
class ColorHolder {
    var r = 0
    var g = 0
    var b = 0
    var a = 0

    constructor(r: Int, g: Int, b: Int) {
        this.r = r
        this.g = g
        this.b = b
        a = 255
    }

    constructor(r: Int, g: Int, b: Int, a: Int) {
        this.r = r
        this.g = g
        this.b = b
        this.a = a
    }

    constructor(color: Color) {
        this.r = color.red
        this.g = color.green
        this.b = color.blue
        this.a = color.alpha
    }

    val brightness = 255f / intArrayOf(r, g, b).max()!!.toFloat()

    fun normalized(): ColorHolder {
        return ColorHolder((r * brightness).toInt(), (b * brightness).toInt(), (g * brightness).toInt(), a)
    }

    fun multiply(multiplier: Float): ColorHolder {
        return ColorHolder((r * multiplier).toInt(), (b * multiplier).toInt(), (g * multiplier).toInt(), a)
    }

    fun setGLColor() {
        glColor4f(this.r / 255f, this.g / 255f, this.b / 255f, this.a / 255f)
    }

    fun toHex(): Int {
        return 0xff shl 24 or (r and 0xff shl 16) or (g and 0xff shl 8) or (b and 0xff)
    }

    fun toJavaColour(): Color {
        return Color(r, g, b, a)
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

    companion object
}