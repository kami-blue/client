package me.zeroeightsix.kami.util.graphics

import me.zeroeightsix.kami.util.math.MathUtils
import kotlin.math.*

object AnimationUtils {
    fun toDeltaTime(startTime: Long) = (System.currentTimeMillis() - startTime).toDouble()

    fun linearInc(deltaTime: Double, length: Double, minValue: Double = 0.0, maxValue: Double = 1.0) =
            MathUtils.convertRange(deltaTime, 0.0, length, minValue, maxValue)

    fun linearDec(deltaTime: Double, length: Double, minValue: Double = 0.0, maxValue: Double = 1.0) =
            MathUtils.convertRange(deltaTime, 0.0, length, maxValue, minValue)

    fun fullSineInc(deltaTime: Double, length: Double, minValue: Double = 0.0, maxValue: Double = 1.0) =
            (cos(deltaTime.coerceIn(0.0, length) * PI * (1.0 / length)) * 0.5 + 0.5) * (maxValue - minValue) + minValue

    fun fullSineDec(deltaTime: Double, length: Double, minValue: Double = 0.0, maxValue: Double = 1.0) =
            (cos(deltaTime.coerceIn(0.0, length) * PI * (1.0 / length)) * -0.5 + 0.5) * (maxValue - minValue) + minValue

    fun halfSineInc(deltaTime: Double, length: Double, minValue: Double = 0.0, maxValue: Double = 1.0) =
            sin(0.5 * deltaTime.coerceIn(0.0, length) * PI * (1.0 / length)) * (maxValue - minValue) + minValue

    fun halfSineDec(deltaTime: Double, length: Double, minValue: Double = 0.0, maxValue: Double = 1.0) =
            cos(0.5 * deltaTime.coerceIn(0.0, length) * PI * (1.0 / length)) * (maxValue - minValue) + minValue

    fun exponentInc(deltaTime: Double, length: Double, minValue: Double = 0.0, maxValue: Double = 1.0) =
            sqrt(1.0 - (deltaTime.coerceIn(0.0, length) / length - 1.0).pow(2)) * (maxValue - minValue) + minValue

    fun exponentDec(deltaTime: Double, length: Double, minValue: Double = 0.0, maxValue: Double = 1.0) =
            sqrt(1.0 - ((deltaTime.coerceIn(0.0, length) + length) / length - 1.0).pow(2)) * (maxValue - minValue) + minValue
}