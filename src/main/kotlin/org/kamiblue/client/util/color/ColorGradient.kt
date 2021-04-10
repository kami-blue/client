package org.kamiblue.client.util.color

import org.kamiblue.commons.utils.MathUtils
import kotlin.math.max
import kotlin.math.roundToInt

class ColorGradient(vararg stops: Pair<Float, ColorHolder>) {
    private val colorArray = stops.sortedBy { it.first }.toTypedArray()

    fun get(valueIn: Float): ColorHolder {
        if (colorArray.isEmpty()) return ColorHolder(255, 255, 255)
        var prevStop = colorArray.last()
        var nextStop = colorArray.last()
        for ((index, pair) in colorArray.withIndex()) {
            if (pair.first < valueIn) continue
            prevStop = if (pair.first == valueIn) pair
            else colorArray[max(index - 1, 0)]
            nextStop = pair
            break
        }
        if (prevStop == nextStop) return prevStop.second
        val r = MathUtils.convertRange(valueIn, prevStop.first, nextStop.first, prevStop.second.r.toFloat(), nextStop.second.r.toFloat()).roundToInt()
        val g = MathUtils.convertRange(valueIn, prevStop.first, nextStop.first, prevStop.second.g.toFloat(), nextStop.second.g.toFloat()).roundToInt()
        val b = MathUtils.convertRange(valueIn, prevStop.first, nextStop.first, prevStop.second.b.toFloat(), nextStop.second.b.toFloat()).roundToInt()
        val a = MathUtils.convertRange(valueIn, prevStop.first, nextStop.first, prevStop.second.a.toFloat(), nextStop.second.a.toFloat()).roundToInt()
        return ColorHolder(r, g, b, a)
    }
}