package org.kamiblue.client.util.color

import org.kamiblue.client.util.color.ColorConverter.hexToRgb
import org.lwjgl.opengl.GL11
import java.awt.Color

class HueCycler(val cycles: Int) {
    private val hueMultiplier = 1f / cycles.toFloat()
    private val colorCycle: Array<Int> = Array(cycles) { i ->
        Color.HSBtoRGB(i * hueMultiplier, 1f, 1f)
    }
    private var index = 0

    fun reset() {
        set(0)
    }

    fun set(indexIn: Int) {
        index = indexIn
    }

    fun setCurrent() {
        val rgb = colorCycle[index]
        val red = (rgb shr 16 and 0xFF) / 255f
        val green = (rgb shr 8 and 0xFF) / 255f
        val blue = (rgb and 0xFF) / 255f
        GL11.glColor3f(red, green, blue)
    }

    fun setNext() {
        setNext(1f)
    }

    fun setNext(a: Float) {
        inc()
        val rgb = currentRgb()
        val r = (rgb.r shr 16 and 0xFF) / 255f
        val g = (rgb.g shr 8 and 0xFF) / 255f
        val b = (rgb.b and 0xFF) / 255f
        GL11.glColor4f(r, g, b, a)
    }

    fun currentInt(): Int {
        return colorCycle[index]
    }

    fun currentRgba(alpha: Int): ColorHolder {
        val color = currentRgb()
        color.a = alpha
        return color
    }

    fun currentRgb(): ColorHolder {
        return hexToRgb(currentInt())
    }

    operator fun plus(plus: Int) {
        index += plus
        if (index >= cycles) index = 0
    }

    operator fun inc(): HueCycler {
        index++
        if (index >= cycles) index = 0
        return this
    }

    init {
        require(cycles > 0) { "cycles <= 0" }
    }
}