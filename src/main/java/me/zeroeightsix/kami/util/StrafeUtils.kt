package me.zeroeightsix.kami.util

import net.minecraft.client.Minecraft
import kotlin.math.*

object StrafeUtils {
    private val mc = Minecraft.getMinecraft()

    /* totally not taken from elytrafly */
    fun getMoveYaw(): Double {
        var strafeYawDeg = 90 * mc.player.moveStrafing
        strafeYawDeg *= if(mc.player.moveForward != 0F)mc.player.moveForward * 0.5F else 1F
        var yawDeg = mc.player.rotationYaw - strafeYawDeg
        yawDeg -= if(mc.player.moveForward < 0F)180 else 0

        return Math.toRadians(yawDeg.toDouble())
    }

    fun getSpeed(): Double {
        return sqrt(mc.player.motionX.pow(2) + mc.player.motionZ.pow(2))
    }

    fun setSpeed(speed: Double) {
        mc.player.motionX = -sin(getMoveYaw()) * speed
        mc.player.motionZ = cos(getMoveYaw()) * speed
    }
}