package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.mixin.extension.x
import me.zeroeightsix.kami.mixin.extension.y
import me.zeroeightsix.kami.mixin.extension.z
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import java.util.*

object Blink : Module(
    name = "Blink",
    category = Category.PLAYER,
    description = "Cancels server side packets"
) {
    private val cancelPacket = setting("CancelPackets", false)
    private val autoReset = setting("AutoReset", true)
    private val resetThreshold = setting("ResetThreshold", 20, 1..100, 5, { autoReset.value })

    private const val ENTITY_ID = -114514
    private val packets = ArrayDeque<CPacketPlayer>()
    private var clonedPlayer: EntityOtherPlayerMP? = null
    private var sending = false

    init {
        listener<PacketEvent.Send> {
            if (!sending && it.packet is CPacketPlayer) {
                it.cancel()
                packets.add(it.packet)
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@safeListener
            if (autoReset.value && packets.size >= resetThreshold.value) {
                end()
                begin()
            }
        }

        listener<ConnectionEvent.Disconnect> {
            mc.addScheduledTask {
                packets.clear()
                clonedPlayer = null
            }
        }
    }

    override fun onEnable() {
        begin()
    }

    override fun onDisable() {
        end()
    }

    private fun begin() {
        if (mc.player == null) return
        clonedPlayer = EntityOtherPlayerMP(mc.world, mc.session.profile).apply {
            copyLocationAndAnglesFrom(mc.player)
            rotationYawHead = mc.player.rotationYawHead
            inventory.copyInventory(mc.player.inventory)
            noClip = true
        }.also {
            mc.world.addEntityToWorld(ENTITY_ID, it)
        }
    }

    private fun end() {
        mc.addScheduledTask {
            val player = mc.player
            val connection = mc.connection
            if (player == null || connection == null) return@addScheduledTask

            if (cancelPacket.value || mc.connection == null) {
                packets.peek()?.let { player.setPosition(it.x, it.y, it.z) }
                packets.clear()
            } else {
                sending = true
                while (packets.isNotEmpty()) connection.sendPacket(packets.poll())
                sending = false
            }

            clonedPlayer?.setDead()
            mc.world?.removeEntityFromWorld(ENTITY_ID)
            clonedPlayer = null
        }
    }

    override fun getHudInfo(): String {
        return packets.size.toString()
    }
}