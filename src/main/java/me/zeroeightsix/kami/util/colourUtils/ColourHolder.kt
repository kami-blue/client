package me.zeroeightsix.kami.util.colourUtils

import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Gebruiker on 18/04/2017.
 * Updated by Xiaro on 09/08/20
 */
class ColourHolder {
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

    fun brighter(): ColourHolder {
        return ColourHolder(min(r + 10, 255), min(g + 10, 255), min(b + 10, 255), a)
    }

    fun darker(): ColourHolder {
        return ColourHolder(max(r - 10, 0), max(g - 10, 0), max(b - 10, 0), a)
    }

    fun setGLColour() {
        setGLColour(-1, -1, -1, -1)
    }

    fun setGLColour(dr: Int, dg: Int, db: Int, da: Int) {
        GL11.glColor4f((if (dr == -1) r else dr).toFloat() / 255, (if (dg == -1) g else dg).toFloat() / 255, (if (db == -1) b else db).toFloat() / 255, (if (da == -1) a else da).toFloat() / 255)
    }

    fun becomeHex(hex: Int) {
        this.r = hex and 0xFF0000 shr 16
        this.g = hex and 0xFF00 shr 8
        this.b = hex and 0xFF
        this.a = 255
    }

    fun fromHex(hex: Int): ColourHolder {
        val n = ColourHolder(0, 0, 0)
        n.becomeHex(hex)
        return n
    }

    fun toHex(): Int {
        return 0xff shl 24 or (r and 0xff shl 16) or (g and 0xff shl 8) or (b and 0xff)
    }

    fun toJavaColour(): Color {
        return Color(r, g, b, a)
    }

    fun clone(): ColourHolder {
        return ColourHolder(r, g, b, a)
    }
}