package org.kamiblue.client.module.modules.movement

import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.event.events.PlayerMoveEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.BaritoneUtils
import org.kamiblue.client.util.MovementUtils
import org.kamiblue.client.util.MovementUtils.applySpeedPotionEffects
import org.kamiblue.client.util.MovementUtils.isInputting
import org.kamiblue.client.util.MovementUtils.setSpeed
import org.kamiblue.client.util.MovementUtils.speed
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.threads.runSafeR
import org.kamiblue.client.util.threads.safeListener

/**
 * @see org.kamiblue.client.mixin.client.player.MixinEntityPlayerSP.setSprinting
 */
internal object Sprint : Module(
    name = "Sprint",
    description = "Automatically makes the player sprint",
    category = Category.MOVEMENT
) {
    private val multiDirection by setting("Multi Direction", true, description = "Sprint in any direction")
    private val instant by setting("Instant", false, description = "Speeds up instantly")
    private val checkFlying by setting("Check Flying", true, description = "Cancels while flying")
    private val checkCollide by setting("Check Collide", true, description = "Cancels on colliding with blocks")
    private val checkCriticals by setting("Check Criticals", false, description = "Cancels on attack for criticals")

    private val attackTimer = TickTimer()

    init {
        safeListener<PacketEvent.Send>(-69420) {
            if (checkCriticals(it)) {
                player.isSprinting = false
                attackTimer.reset()
                connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SPRINTING))
            }
        }

        safeListener<PlayerMoveEvent> {
            if (instant && player.onGround && player.isSprinting && !player.isSneaking && isInputting) {
                val speed = player.speed
                val targetSpeed = applySpeedPotionEffects(0.2805)

                if (speed > 0.0 && speed < targetSpeed) {
                    val multiplier = targetSpeed / speed
                    player.motionX *= multiplier
                    player.motionZ *= multiplier
                }
            }
        }
    }

    private fun SafeClientEvent.checkCriticals(event: PacketEvent) =
        checkCriticals
            && !event.cancelled
            && event.packet is CPacketUseEntity
            && event.packet.action == CPacketUseEntity.Action.ATTACK
            && player.getCooledAttackStrength(0.5f) > 0.9f
            && event.packet.getEntityFromWorld(world) is EntityLivingBase

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase == TickEvent.Phase.START) {
                player.isSprinting = shouldSprint()
            }
        }
    }

    @JvmStatic
    fun shouldSprint() =
        runSafeR {
            !mc.gameSettings.keyBindSneak.isKeyDown
                && !player.isElytraFlying
                && player.foodStats.foodLevel > 6
                && !BaritoneUtils.isPathing
                && checkMovementInput()
                && (!checkFlying || !player.capabilities.isFlying)
                && (!checkCollide || !player.collidedHorizontally)
                && (!checkCriticals || attackTimer.tick(100L, false))
        } ?: false

    private fun SafeClientEvent.checkMovementInput() =
        if (multiDirection) MovementUtils.isInputting else
            player.movementInput.moveForward > 0.0f
}