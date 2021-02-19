package org.kamiblue.client.module.modules.combat

import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.MovementUtils.setSpeed
import org.kamiblue.client.util.MovementUtils.speed
import org.kamiblue.client.util.threads.safeListener

internal object Criticals : Module(
    name = "Criticals",
    category = Category.COMBAT,
    description = "Always do critical attacks"
) {
    private val mode = setting("Mode", CriticalMode.PACKET)
    private val miniJump = setting("Mini Jump", true, { mode.value == CriticalMode.DELAY })

    private enum class CriticalMode {
        PACKET, DELAY
    }

    private var delayTick = 0
    private var sendingPacket = false
    private var attackPacket = CPacketUseEntity()
    private var swingPacket = CPacketAnimation()

    init {
        onDisable {
            delayTick = 0
        }

        safeListener<PacketEvent.Send> {
            if (it.packet !is CPacketAnimation && it.packet !is CPacketUseEntity) return@safeListener

            if (player.isInWater || player.isInLava || !player.onGround) return@safeListener /* Don't run if player is sprinting or weapon is still in cooldown */

            if (it.packet is CPacketUseEntity && it.packet.action == CPacketUseEntity.Action.ATTACK) {
                val target = it.packet.getEntityFromWorld(world)
                if (target == null || target !is EntityLivingBase) return@safeListener
                player.isSprinting = false
                if (player.speed > 0.2) setSpeed(0.2)
                if (mode.value == CriticalMode.PACKET) {
                    packetMode()
                } else {
                    delayModeAttack(it)
                }
            } else if (it.packet is CPacketAnimation && mode.value == CriticalMode.DELAY) {
                delayModeSwing(it)
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            /* Sends attack packet and swing packet when falling */
            if (mode.value == CriticalMode.DELAY && delayTick != 0) {
                player.isSprinting = false
                if (player.speed > 0.2) setSpeed(0.2)
                if (player.motionY < -0.1 && delayTick in 1..15) {
                    sendingPacket = true
                    connection.sendPacket(attackPacket)
                    connection.sendPacket(swingPacket)
                    delayTick = 16
                }
                if (delayTick in 1..19) {
                    delayTick++
                } else if (delayTick > 19) { /* Resets after 1 second if no packets sent */
                    delayTick = 0
                }
            }
        }
    }

    private fun SafeClientEvent.packetMode() {
        /* lol Minecraft checks for criticals if you're not on a block so just say you're not */
        connection.sendPacket(CPacketPlayer.Position(player.posX, player.posY + 0.1f, player.posZ, false))
        connection.sendPacket(CPacketPlayer.Position(player.posX, player.posY, player.posZ, false))
    }

    private fun delayModeAttack(event: PacketEvent) {
        /* Cancels attack packet and jump when you hit an entity */
        val packet = event.packet as CPacketUseEntity
        if (delayTick == 0) {
            attackPacket = packet
            event.cancel()
            delayTick = 1
            mc.player.jump()
            if (miniJump.value) {
                mc.player.motionY = 0.25
            }
        } else if (!sendingPacket) {
            event.cancel()
        }
    }

    private fun delayModeSwing(event: PacketEvent) {
        /* Cancels swing packet after attack packet was cancelled */
        if (delayTick != 0) {
            if (delayTick == 1) {
                swingPacket = event.packet as CPacketAnimation
                event.cancel()
            } else if (!sendingPacket) {
                event.cancel()
            } else {
                sendingPacket = false
            }
        }
    }
}