package me.zeroeightsix.kami.util

import net.minecraft.client.Minecraft
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

object MovementUtils {
    private val mc = Minecraft.getMinecraft()

    /* totally not taken from elytrafly */
    fun calcMoveYaw(yawIn: Float = mc.player.rotationYaw, moveForward: Float = roundedForward, moveString: Float = roundedStrafing): Double {
        var strafe = 90 * moveString
        strafe *= if (moveForward != 0F) moveForward * 0.5F else 1F
        var yaw = yawIn - strafe
        yaw -= if (moveForward < 0F) 180 else 0

        return Math.toRadians(yaw.toDouble())
    }

    private val roundedForward get() = getRoundedMovementInput(mc.player.movementInput.moveForward)

    private val roundedStrafing get() = getRoundedMovementInput(mc.player.movementInput.moveStrafe)

    private fun getRoundedMovementInput(input: Float) = when {
        input > 0f -> 1f
        input < 0f -> -1f
        else -> 0f
    }

    val isMoving get() = getSpeed() > 0.0001

    /* triangle math tho */
    fun getSpeed(): Double {
        return hypot(mc.player.motionX, mc.player.motionZ)
    }

    fun setSpeed(speed: Double) {
        val yaw = calcMoveYaw()
        mc.player.motionX = -sin(yaw) * speed
        mc.player.motionZ = cos(yaw) * speed
    }

    fun isInputing() = mc.player.movementInput.moveForward != 0f || mc.player.movementInput.moveStrafe != 0f
}