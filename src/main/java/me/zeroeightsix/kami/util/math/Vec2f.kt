package me.zeroeightsix.kami.util.math

import net.minecraft.entity.Entity
import kotlin.math.hypot
import kotlin.math.pow

data class Vec2f(val x: Float, val y: Float) {

    /**
     * Create a Vec2f from this entity's rotations
     */
    constructor(entity: Entity) : this(entity.rotationYaw, entity.rotationPitch)

    constructor(vec2d: Vec2d) : this(vec2d.x.toFloat(), vec2d.y.toFloat())

    fun toRadians(): Vec2f {
        return Vec2f(this.x / 180.0f * PI, this.y / 180.0f * PI)
    }


    fun length() = hypot(x, y)

    fun lengthSquared() = (x.pow(2) + y.pow(2))


    operator fun div(vec2f: Vec2f) = div(vec2f.x, vec2f.y)

    operator fun div(divider: Float) = div(divider, divider)

    fun div(x: Float, y: Float) = Vec2f(this.x / x, this.y / y)


    operator fun times(vec2f: Vec2f) = times(vec2f.x, vec2f.y)

    operator fun times(multiplier: Float) = times(multiplier, multiplier)

    fun times(x: Float, y: Float) = Vec2f(this.x * x, this.y * y)


    operator fun minus(vec2f: Vec2f) = minus(vec2f.x, vec2f.y)

    operator fun minus(value: Float) = minus(value, value)

    fun minus(x: Float, y: Float) = plus(-x, -y)


    operator fun plus(vec2f: Vec2f) = plus(vec2f.x, vec2f.y)

    operator fun plus(value: Float) = plus(value, value)

    fun plus(x: Float, y: Float) = Vec2f(this.x + x, this.y + y)

    companion object {
        val ZERO get() = Vec2f(0f, 0f)

        private const val PI = 3.14159265358979323846f
    }
}