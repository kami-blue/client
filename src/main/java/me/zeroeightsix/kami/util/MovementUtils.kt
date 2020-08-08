package me.zeroeightsix.kami.util

import net.minecraft.client.Minecraft
import kotlin.math.*

object MovementUtils {
    private val mc = Minecraft.getMinecraft()

    /* totally not taken from elytrafly */
    fun getMoveYaw(): Double {
        var strafe = 90 * mc.player.moveStrafing
        strafe *= if(mc.player.moveForward != 0F)mc.player.moveForward * 0.5F else 1F
        var yaw = mc.player.rotationYaw - strafe
        yaw -= if(mc.player.moveForward < 0F)180 else 0

        return Math.toRadians(yaw.toDouble())
    }

    /* triangle math tho */
    fun getSpeed(): Double {
        return sqrt(mc.player.motionX.pow(2) + mc.player.motionZ.pow(2))
    }

    fun setSpeed(speed: Double) {
        mc.player.motionX = -sin(getMoveYaw()) * speed
        mc.player.motionZ = cos(getMoveYaw()) * speed
    }
}