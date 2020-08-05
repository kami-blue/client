package me.zeroeightsix.kami.util

import net.minecraft.client.Minecraft
import kotlin.math.*

object StrafeUtils {
    private val mc = Minecraft.getMinecraft()

    fun getDirection(): Double {
        return Math.toRadians(mc.player.rotationYaw.toDouble())
    }

    fun getSpeed(): Double {
        return sqrt(mc.player.motionX.pow(2) + mc.player.motionZ.pow(2))
    }

    fun setSpeed(speed: Double) {
        mc.player.motionX = -sin(getDirection()) * speed
        mc.player.motionZ = cos(getDirection()) * speed
    }
}
