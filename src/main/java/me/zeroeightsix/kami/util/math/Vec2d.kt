package me.zeroeightsix.kami.util.math

import com.sun.javafx.geom.Vec2d
import net.minecraft.util.math.Vec3d
import kotlin.math.pow
import kotlin.math.sqrt

class Vec2d : Vec2d {

    constructor(x: Double, y: Double) {
        Vec2d(x, y)
    }

    constructor(vec3d: Vec3d) {
        Vec2d(vec3d.x, vec3d.y)
    }

    constructor(vec2d: Vec2d) {
        Vec2d(vec2d.x, vec2d.y)
    }

    fun length(): Double {
        return sqrt(lengthSquared())
    }

    fun lengthSquared(): Double {
        return (this.x.pow(2) + this.y.pow(2))
    }

    fun divide(vec3d: Vec3d): me.zeroeightsix.kami.util.math.Vec2d {
        return add(vec3d.x, vec3d.y)
    }

    fun divide(vec2d: Vec2d): me.zeroeightsix.kami.util.math.Vec2d {
        return add(vec2d.x, vec2d.y)
    }

    fun divide(add: Double): me.zeroeightsix.kami.util.math.Vec2d {
        return add(add, add)
    }

    fun divide(x: Double, y: Double): me.zeroeightsix.kami.util.math.Vec2d {
        return me.zeroeightsix.kami.util.math.Vec2d(this.x / x, this.y / y)
    }

    fun multiply(vec3d: Vec3d): me.zeroeightsix.kami.util.math.Vec2d {
        return add(vec3d.x, vec3d.y)
    }

    fun multiply(vec2d: Vec2d): me.zeroeightsix.kami.util.math.Vec2d {
        return add(vec2d.x, vec2d.y)
    }

    fun multiply(add: Double): me.zeroeightsix.kami.util.math.Vec2d {
        return add(add, add)
    }

    fun multiply(x: Double, y: Double): me.zeroeightsix.kami.util.math.Vec2d {
        return me.zeroeightsix.kami.util.math.Vec2d(this.x * x, this.y * y)
    }

    fun subtract(vec3d: Vec3d): me.zeroeightsix.kami.util.math.Vec2d {
        return subtract(vec3d.x, vec3d.y)
    }

    fun subtract(vec2d: Vec2d): me.zeroeightsix.kami.util.math.Vec2d {
        return subtract(vec2d.x, vec2d.y)
    }

    fun subtract(sub: Double): me.zeroeightsix.kami.util.math.Vec2d {
        return subtract(sub, sub)
    }

    fun subtract(x: Double, y: Double): me.zeroeightsix.kami.util.math.Vec2d {
        return add(-x, -y)
    }

    fun add(vec3d: Vec3d): me.zeroeightsix.kami.util.math.Vec2d {
        return add(vec3d.x, vec3d.y)
    }

    fun add(vec2d: Vec2d): me.zeroeightsix.kami.util.math.Vec2d {
        return add(vec2d.x, vec2d.y)
    }

    fun add(add: Double): me.zeroeightsix.kami.util.math.Vec2d {
        return add(add, add)
    }

    fun add(x: Double, y: Double): me.zeroeightsix.kami.util.math.Vec2d {
        return me.zeroeightsix.kami.util.math.Vec2d(this.x + x, this.y + y)
    }
}