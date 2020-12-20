package me.zeroeightsix.kami.util.math

import net.minecraft.util.math.Vec3d
import kotlin.math.pow
import kotlin.math.sqrt

class Vec2d(var x: Double = 0.0, var y: Double = 0.0) {

    constructor(vec3d: Vec3d) : this(vec3d.x, vec3d.y)

    constructor(vec2d: Vec2d) : this(vec2d.x, vec2d.y)

    fun toRadians(): Vec2d {
        return Vec2d(this.x / 180.0 * Math.PI, this.y / 180.0 * Math.PI)
    }

    fun length(): Double {
        return sqrt(lengthSquared())
    }

    fun lengthSquared(): Double {
        return (this.x.pow(2) + this.y.pow(2))
    }

    fun divide(vec3d: Vec3d): Vec2d {
        return divide(vec3d.x, vec3d.y)
    }

    fun divide(vec2d: Vec2d): Vec2d {
        return divide(vec2d.x, vec2d.y)
    }

    fun divide(divider: Double): Vec2d {
        return divide(divider, divider)
    }

    fun divide(x: Double, y: Double): Vec2d {
        return Vec2d(this.x / x, this.y / y)
    }

    fun multiply(vec3d: Vec3d): Vec2d {
        return multiply(vec3d.x, vec3d.y)
    }

    fun multiply(vec2d: Vec2d): Vec2d {
        return multiply(vec2d.x, vec2d.y)
    }

    fun multiply(mulitplier: Double): Vec2d {
        return multiply(mulitplier, mulitplier)
    }

    fun multiply(x: Double, y: Double): Vec2d {
        return Vec2d(this.x * x, this.y * y)
    }

    fun subtract(vec3d: Vec3d): Vec2d {
        return subtract(vec3d.x, vec3d.y)
    }

    fun subtract(vec2d: Vec2d): Vec2d {
        return subtract(vec2d.x, vec2d.y)
    }

    fun subtract(sub: Double): Vec2d {
        return subtract(sub, sub)
    }

    fun subtract(x: Double, y: Double): Vec2d {
        return add(-x, -y)
    }

    fun add(vec3d: Vec3d): Vec2d {
        return add(vec3d.x, vec3d.y)
    }

    fun add(vec2d: Vec2d): Vec2d {
        return add(vec2d.x, vec2d.y)
    }

    fun add(add: Double): Vec2d {
        return add(add, add)
    }

    fun add(x: Double, y: Double): Vec2d {
        return Vec2d(this.x + x, this.y + y)
    }

    override fun toString(): String {
        return "Vec2d[${this.x}, ${this.y}]"
    }
}