package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MovementUtils
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketUseEntity

@Module.Info(
        name = "Criticals",
        category = Module.Category.COMBAT,
        description = "Always do critical attacks"
)
object Criticals : Module() {
    private val mode = register(Settings.e<CriticalMode>("Mode", CriticalMode.PACKET))
    private val miniJump = register(Settings.booleanBuilder("MiniJump").withValue(true).withVisibility { mode.value == CriticalMode.DELAY }.build())

    private enum class CriticalMode {
        PACKET, DELAY
    }

    private var delayTick = 0
    private var sendingPacket = false
    private var attackPacket = CPacketUseEntity()
    private var swingPacket = CPacketAnimation()

    init {
        listener<PacketEvent.Send> {
            if (mc.player == null || !(it.packet is CPacketAnimation || it.packet is CPacketUseEntity)) return@listener
            if (mc.player.isInWater || mc.player.isInLava || !mc.player.onGround) return@listener /* Don't run if player is sprinting or weapon is still in cooldown */

            if (it.packet is CPacketUseEntity && it.packet.action == CPacketUseEntity.Action.ATTACK) {
                val target = it.packet.getEntityFromWorld(mc.world)
                if (target == null || target !is EntityLivingBase) return@listener
                mc.player.isSprinting = false
                if (MovementUtils.getSpeed() > 0.2) MovementUtils.setSpeed(0.2)
                if (mode.value == CriticalMode.PACKET) {
                    packetMode()
                } else {
                    delayModeAttack(it)
                }
            } else if (it.packet is CPacketAnimation && mode.value == CriticalMode.DELAY) {
                delayModeSwing(it)
            }
        }

        listener<SafeTickEvent> {
            /* Sends attack packet and swing packet when falling */
            if (mode.value == CriticalMode.DELAY && delayTick != 0) {
                mc.player.isSprinting = false
                if (MovementUtils.getSpeed() > 0.2) MovementUtils.setSpeed(0.2)
                if (mc.player.motionY < -0.1 && delayTick in 1..15) {
                    sendingPacket = true
                    mc.connection!!.sendPacket(attackPacket)
                    mc.connection!!.sendPacket(swingPacket)
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

    private fun packetMode() {
        /* lol Minecraft checks for criticals if you're not on a block so just say you're not */
        mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1f, mc.player.posZ, false))
        mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false))
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

    override fun onDisable() {
        delayTick = 0
    }
}