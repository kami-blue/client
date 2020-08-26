package me.zeroeightsix.kami.util.color

import org.lwjgl.opengl.GL11.glColor4f
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

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

    fun brighter(): ColorHolder {
        return ColorHolder(min(r + 10, 255), min(g + 10, 255), min(b + 10, 255), a)
    }

    fun darker(): ColorHolder {
        return ColorHolder(max(r - 10, 0), max(g - 10, 0), max(b - 10, 0), a)
    }

    fun setGLColor() {
        glColor4f(this.r / 255f, this.g / 255f, this.b / 255f, this.a / 255f)
    }

    fun becomeHex(hex: Int) {
        this.r = hex and 0xFF0000 shr 16
        this.g = hex and 0xFF00 shr 8
        this.b = hex and 0xFF
        this.a = 255
    }

    fun fromHex(hex: Int): ColorHolder {
        val n = ColorHolder(0, 0, 0)
        n.becomeHex(hex)
        return n
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
}