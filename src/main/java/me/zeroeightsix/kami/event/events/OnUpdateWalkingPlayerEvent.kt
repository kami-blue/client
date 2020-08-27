package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d

class OnUpdateWalkingPlayerEvent(
        private var moving: Boolean,
        private var rotating: Boolean,
        var sprinting: Boolean,
        var sneaking: Boolean,
        var onGround: Boolean,
        private val initPos: Vec3d,
        private val initRotation: Vec2f
) : KamiEvent() {

    var pos: Vec3d = initPos
        set(value) {
            if (moving || value.subtract(initPos).lengthSquared() > 0.0009) {
                moving = true
                field = value
            }
        }

    var rotation: Vec2f = initRotation
        set(value) {
            if (rotating || value.x - initRotation.x != 0f || value.y - initRotation.y != 0f) {
                rotating = true
                field = value
            }
        }

    fun getMoving(): Boolean {
        return moving
    }

    fun getRotating(): Boolean {
        return rotating
    }

}