package org.kamiblue.client.util

import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.init.MobEffects
import net.minecraft.util.MovementInput
import net.minecraft.util.math.BlockPos
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.util.EntityUtils.flooredPosition
import org.kamiblue.client.util.math.VectorUtils.toVec3d
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

object MovementUtils {
    private val mc = Minecraft.getMinecraft()

    val isInputting
        get() = mc.player?.movementInput?.let {
            it.moveForward != 0.0f || it.moveStrafe != 0.0f
        } ?: false

    val Entity.isMoving get() = speed > 0.0001
    val Entity.speed get() = hypot(motionX, motionZ)
    val Entity.realSpeed get() = hypot(posX - prevPosX, posZ - prevPosZ)

    /* totally not taken from elytrafly */
    fun SafeClientEvent.calcMoveYaw(yawIn: Float = mc.player.rotationYaw, moveForward: Float = roundedForward, moveString: Float = roundedStrafing): Double {
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

    fun SafeClientEvent.setSpeed(speed: Double) {
        val yaw = calcMoveYaw()
        player.motionX = -sin(yaw) * speed
        player.motionZ = cos(yaw) * speed
    }

    fun SafeClientEvent.applySpeedPotionEffects(speed: Double) =
        player.getActivePotionEffect(MobEffects.SPEED)?.let {
            speed * (1.0 + (it.amplifier + 1) * 0.2)
        } ?: speed

    fun EntityPlayerSP.centerPlayer(): Boolean {
        val center = this.flooredPosition.toVec3d(0.5, 0.0, 0.5)
        val centered = isCentered(this.flooredPosition)

        if (!centered) {
            this.motionX = (center.x - this.posX) / 2.0
            this.motionZ = (center.z - this.posZ) / 2.0

            val speed = this.speed

            if (speed > 0.2805) {
                val multiplier = 0.2805 / speed
                this.motionX *= multiplier
                this.motionZ *= multiplier
            }
        }

        return centered
    }

    fun Entity.isCentered(pos: BlockPos) =
        this.posX in pos.x + 0.31..pos.x + 0.69
            && this.posZ in pos.z + 0.31..pos.z + 0.69

    fun MovementInput.resetMove() {
        moveForward = 0.0f
        moveStrafe = 0.0f
        forwardKeyDown = false
        backKeyDown = false
        leftKeyDown = false
        rightKeyDown = false
    }

    fun MovementInput.resetJumpSneak() {
        jump = false
        sneak = false
    }
}