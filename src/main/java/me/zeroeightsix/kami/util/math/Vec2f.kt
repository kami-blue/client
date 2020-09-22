package me.zeroeightsix.kami.util.math

import net.minecraft.entity.Entity
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * @author Xiaro
 *
 * Created by Xiaro on 27/08/20
 */
class Vec2f(@JvmField var x: Float, @JvmField var y: Float) {

    /**
     * Create a Vec2f from this entity's rotations
     */
    constructor(entity: Entity) : this(entity.rotationYaw, entity.rotationPitch)

    fun toRadians(): Vec2d {
        return Vec2d(this.x / 180.0 * Math.PI, this.y / 180.0 * Math.PI)
    }

    fun length(): Float {
        return sqrt(lengthSquared())
    }

    fun lengthSquared(): Float {
        return (this.x.pow(2) + this.y.pow(2))
    }

    fun divide(vec2f: Vec2f): Vec2f {
        return divide(vec2f.x, vec2f.y)
    }

    fun divide(divider: Float): Vec2f {
        return divide(divider, divider)
    }

    fun divide(x: Float, y: Float): Vec2f {
        return Vec2f(this.x / x, this.y / y)
    }

    fun multiply(vec2f: Vec2f): Vec2f {
        return multiply(vec2f.x, vec2f.y)
    }

    fun multiply(mulitplier: Float): Vec2f {
        return multiply(mulitplier, mulitplier)
    }

    fun multiply(x: Float, y: Float): Vec2f {
        return Vec2f(this.x * x, this.y * y)
    }

    fun subtract(vec2f: Vec2f): Vec2f {
        return subtract(vec2f.x, vec2f.y)
    }

    fun subtract(sub: Float): Vec2f {
        return subtract(sub, sub)
    }

    fun subtract(x: Float, y: Float): Vec2f {
        return add(-x, -y)
    }

    fun add(vec2f: Vec2f): Vec2f {
        return add(vec2f.x, vec2f.y)
    }

    fun add(add: Float): Vec2f {
        return add(add, add)
    }

    fun add(x: Float, y: Float): Vec2f {
        return Vec2f(this.x + x, this.y + y)
    }
}