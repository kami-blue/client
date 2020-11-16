package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*

@Module.Info(
        name = "Blink",
        category = Module.Category.PLAYER,
        description = "Cancels server side packets"
)
object Blink : Module() {
    private val cancelPacket = setting("CancelPackets", false)
    private val autoReset = setting("AutoReset", true)
    private val resetThreshold = setting("ResetThreshold", 20, 1..100, 5, { autoReset.value })

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

        listener<SafeTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@listener
            if (autoReset.value && packets.size >= resetThreshold.value) {
                end()
                begin()
            }
        }

        listener<ConnectionEvent.Disconnect> {
            packets.clear()
            clonedPlayer = null
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
            mc.world.addEntityToWorld(-114514, it)
        }
    }

    private fun end() {
        if (mc.player == null) return
        if (cancelPacket.value || mc.connection == null) {
            packets.peek()?.let { mc.player.setPosition(it.x, it.y, it.z) }
            packets.clear()
        } else {
            sending = true
            while (packets.isNotEmpty()) mc.connection!!.sendPacket(packets.poll())
            sending = false
        }
        clonedPlayer?.setDead()
        clonedPlayer = null
    }

    override fun getHudInfo(): String {
        return packets.size.toString()
    }
}