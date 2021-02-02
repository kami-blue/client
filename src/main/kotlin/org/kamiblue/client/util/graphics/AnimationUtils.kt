package org.kamiblue.client.util.graphics

import org.kamiblue.commons.utils.MathUtils
import kotlin.math.*

object AnimationUtils {
    private const val PI_FLOAT = PI.toFloat()

    fun toDeltaTimeFloat(startTime: Long) = (System.currentTimeMillis() - startTime).toFloat()

    fun toDeltaTimeDouble(startTime: Long) = (System.currentTimeMillis() - startTime).toDouble()

    // Linear
    // Float
    fun linear(deltaTime: Float, length: Float, from: Float, to: Float) =
        if (from < to) linearInc(deltaTime, length, from, to)
        else linearDec(deltaTime, length, to, from)

    fun linearInc(deltaTime: Float, length: Float, minValue: Float = 0.0f, maxValue: Float = 1.0f) =
        MathUtils.convertRange(deltaTime, 0.0f, length, minValue, maxValue)

    fun linearDec(deltaTime: Float, length: Float, minValue: Float = 0.0f, maxValue: Float = 1.0f) =
        MathUtils.convertRange(deltaTime, 0.0f, length, maxValue, minValue)

    // Double
    fun linear(deltaTime: Double, length: Double, from: Double, to: Double) =
        if (from < to) linearInc(deltaTime, length, from, to)
        else linearDec(deltaTime, length, to, from)

    fun linearInc(deltaTime: Double, length: Double, minValue: Double = 0.0, maxValue: Double = 1.0) =
        MathUtils.convertRange(deltaTime, 0.0, length, minValue, maxValue)

    fun linearDec(deltaTime: Double, length: Double, minValue: Double = 0.0, maxValue: Double = 1.0) =
        MathUtils.convertRange(deltaTime, 0.0, length, maxValue, minValue)


    // Sine
    // Float
    fun sine(deltaTime: Float, length: Float, from: Float, to: Float) =
        if (from < to) halfSineInc(deltaTime, length, from, to)
        else halfSineDec(deltaTime, length, to, from)

    fun fullSineInc(deltaTime: Float, length: Float, minValue: Float = 0.0f, maxValue: Float = 1.0f) =
        (cos(deltaTime.coerceIn(0.0f, length) * PI_FLOAT * (1.0f / length)) * 0.5f + 0.5f) * (maxValue - minValue) + minValue

    fun fullSineDec(deltaTime: Float, length: Float, minValue: Float = 0.0f, maxValue: Float = 1.0f) =
        (cos(deltaTime.coerceIn(0.0f, length) * PI_FLOAT * (1.0f / length)) * -0.5f + 0.5f) * (maxValue - minValue) + minValue

    fun halfSineInc(deltaTime: Float, length: Float, minValue: Float = 0.0f, maxValue: Float = 1.0f) =
        sin(0.5f * deltaTime.coerceIn(0.0f, length) * PI_FLOAT * (1.0f / length)) * (maxValue - minValue) + minValue

    fun halfSineDec(deltaTime: Float, length: Float, minValue: Float = 0.0f, maxValue: Float = 1.0f) =
        cos(0.5f * deltaTime.coerceIn(0.0f, length) * PI_FLOAT * (1.0f / length)) * (maxValue - minValue) + minValue

    // Double
    fun sine(deltaTime: Double, length: Double, from: Double, to: Double) =
        if (from < to) halfSineInc(deltaTime, length, from, to)
        else halfSineDec(deltaTime, length, to, from)

    fun fullSineInc(deltaTime: Double, length: Double, minValue: Double = 0.0, maxValue: Double = 1.0) =
        (cos(deltaTime.coerceIn(0.0, length) * PI * (1.0 / length)) * 0.5 + 0.5) * (maxValue - minValue) + minValue

    fun fullSineDec(deltaTime: Double, length: Double, minValue: Double = 0.0, maxValue: Double = 1.0) =
        (cos(deltaTime.coerceIn(0.0, length) * PI * (1.0 / length)) * -0.5 + 0.5) * (maxValue - minValue) + minValue

    fun halfSineInc(deltaTime: Double, length: Double, minValue: Double = 0.0, maxValue: Double = 1.0) =
        sin(0.5 * deltaTime.coerceIn(0.0, length) * PI * (1.0 / length)) * (maxValue - minValue) + minValue

    fun halfSineDec(deltaTime: Double, length: Double, minValue: Double = 0.0, maxValue: Double = 1.0) =
        cos(0.5 * deltaTime.coerceIn(0.0, length) * PI * (1.0 / length)) * (maxValue - minValue) + minValue


    // Exponent
    // Float
    fun exponent(deltaTime: Float, length: Float, from: Float, to: Float) =
        if (from < to) exponentInc(deltaTime, length, from, to)
        else exponentDec(deltaTime, length, to, from)

    fun exponentInc(deltaTime: Float, length: Float, minValue: Float = 0.0f, maxValue: Float = 1.0f) =
        sqrt(1.0f - (deltaTime.coerceIn(0.0f, length) / length - 1.0f).pow(2)) * (maxValue - minValue) + minValue

    fun exponentDec(deltaTime: Float, length: Float, minValue: Float = 0.0f, maxValue: Float = 1.0f) =
        sqrt(1.0f - ((deltaTime.coerceIn(0.0f, length) + length) / length - 1.0f).pow(2)) * (maxValue - minValue) + minValue

    // Double
    fun exponent(deltaTime: Double, length: Double, from: Double, to: Double) =
        if (from < to) exponentInc(deltaTime, length, from, to)
        else exponentDec(deltaTime, length, to, from)

    fun exponentInc(deltaTime: Double, length: Double, minValue: Double = 0.0, maxValue: Double = 1.0) =
        sqrt(1.0 - (deltaTime.coerceIn(0.0, length) / length - 1.0).pow(2)) * (maxValue - minValue) + minValue

    fun exponentDec(deltaTime: Double, length: Double, minValue: Double = 0.0, maxValue: Double = 1.0) =
        sqrt(1.0 - ((deltaTime.coerceIn(0.0, length) + length) / length - 1.0).pow(2)) * (maxValue - minValue) + minValue
}