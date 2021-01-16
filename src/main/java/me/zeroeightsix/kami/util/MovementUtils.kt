package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.event.SafeClientEvent
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import org.kamiblue.commons.extension.toRadian
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

object MovementUtils {
    private val mc = Minecraft.getMinecraft()

    val isInputting get() = mc.player?.movementInput?.let {
        it.moveForward != 0f || it.moveStrafe != 0f
    } ?: false

    val Entity.isMoving get() = speed > 0.0001
    val Entity.speed get() = hypot(motionX, motionZ)
    val Entity.realSpeed get() = hypot(posX - prevPosX, posZ - prevPosZ)

    /* totally not taken from elytrafly */
    fun SafeClientEvent.calcMoveYawRad(yawIn: Float = player.rotationYaw, moveForward: Float = roundedForward, moveString: Float = roundedStrafing): Double {
        return calcMoveYawDeg(yawIn, moveForward, moveString).toRadian()
    }

    fun SafeClientEvent.calcMoveYawDeg(yawIn: Float = player.rotationYaw, moveForward: Float = roundedForward, moveString: Float = roundedStrafing): Double {
        var strafe = 90.0 * moveString
        strafe *= if (moveForward != 0.0f) moveForward * 0.5 else 1.0

        var yaw = yawIn - strafe
        yaw -= if (moveForward < 0.0f) 180.0 else 0.0

        return yaw
    }

    private val SafeClientEvent.roundedForward get() = getRoundedMovementInput(player.movementInput.moveForward)
    private val SafeClientEvent.roundedStrafing get() = getRoundedMovementInput(player.movementInput.moveStrafe)

    private fun getRoundedMovementInput(input: Float) = when {
        input > 0f -> 1f
        input < 0f -> -1f
        else -> 0f
    }

    fun SafeClientEvent.setSpeed(speed: Double) {
        val yaw = calcMoveYawRad()
        player.motionX = -sin(yaw) * speed
        player.motionZ = cos(yaw) * speed
    }
}