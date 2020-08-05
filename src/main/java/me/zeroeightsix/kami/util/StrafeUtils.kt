package me.zeroeightsix.kami.util

import net.minecraft.client.Minecraft
import kotlin.math.*

class StrafeUtils {
    fun getDirection(): Double {
        return Math.toRadians(Minecraft.getMinecraft().player.rotationYaw.toDouble())
    }

    fun getSpeed(): Double {
        return sqrt(Minecraft.getMinecraft().player.motionX.pow(2) + Minecraft.getMinecraft().player.motionZ.pow(2))
    }

    fun setSpeed(speed: Double) {
        Minecraft.getMinecraft().player.motionX = -sin(getDirection()) * speed
        Minecraft.getMinecraft().player.motionZ = cos(getDirection()) * speed
    }
}