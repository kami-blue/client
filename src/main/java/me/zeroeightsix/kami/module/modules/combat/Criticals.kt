package me.zeroeightsix.kami.module.modules.combat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraftforge.event.entity.player.AttackEntityEvent

/**
 * @author dominikaaaa
 * Thanks cookie for teaching me how dumb minecraft is uwu
 * Updated by Xiaro on 24/06/20
 */
@Module.Info(
        name = "Criticals",
        category = Module.Category.COMBAT,
        description = "Always do critical attacks"
)
class Criticals : Module() {
    private val mode = register(Settings.e<CriticalMode>("Mode", CriticalMode.PACKET))

    private enum class CriticalMode {
        PACKET, DELAY
    }

    private var delayTick = 0
    private var attackPacket = CPacketUseEntity()
    private var swingPacket = CPacketAnimation()

    @EventHandler
    private val attackEntityEventListener = Listener(EventHook { event: AttackEntityEvent ->
        if (!mc.player.isInWater && !mc.player.isInLava) {
            if (mode.value == CriticalMode.PACKET && mc.player.onGround) { /* lol Minecraft checks for criticals if you're not on a block so just say you're not */
                mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1f, mc.player.posZ, false))
                mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false))
                mc.player.onCriticalHit(event.target)
            } else if (mode.value == CriticalMode.DELAY && !mc.player.isSprinting && mc.player.getCooledAttackStrength(0.0f) >= 1) {
                event.isCanceled = true
            } /* Cancel AttackEntityEvent for compatibility with some modules */
        }
    })

    /* Delay mode code is here because packets are sent before AttackEntityEvent is called for some reasons */
    @EventHandler
    private val sendListener = Listener(EventHook { event: PacketEvent.Send ->
        if (mode.value != CriticalMode.DELAY || !(event.packet is CPacketAnimation || event.packet is CPacketUseEntity)) return@EventHook
        /* Don't run if player is sprinting or weapon is still in cooldown */
        val shouldDoCritical = !mc.player.isInWater && !mc.player.isInLava && !mc.player.isSprinting && mc.player.onGround && mc.player.getCooledAttackStrength(0.0f) >= 1

        /* Cancels attack packet and jump when you hit an entity */
        if (event.packet is CPacketUseEntity && shouldDoCritical && delayTick == 0) {
            val packet = event.packet as CPacketUseEntity
            if (packet.action == CPacketUseEntity.Action.ATTACK) {
                attackPacket = event.packet as CPacketUseEntity
                event.cancel()
                mc.player.jump()
                delayTick = 1
            }
        }
        /* Cancels swing packet after attack packet was cancelled */
        if (event.packet is CPacketAnimation && delayTick == 1) {
            swingPacket = event.packet as CPacketAnimation
            event.cancel()
        }
    })

    override fun onUpdate() {
        /* Sends attack packet and swing packet when falling */
        if (mc.player.motionY < -0.1 && delayTick >= 1) {
            delayTick = 2
            mc.connection!!.sendPacket(attackPacket)
            mc.connection!!.sendPacket(swingPacket)
            delayTick = 0
        } else if (delayTick in 1..19) {
            delayTick++
        } else if (delayTick >= 20) { /* Resets after 1 second if no packets sent */
            delayTick = 0
        }
    }

    override fun onDisable() {
        delayTick = 0
    }
}