package me.zeroeightsix.kami.module.modules.player

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.CPacketPlayer
import java.util.*

/**
 * Created by 086 on 24/01/2018.
 * Updated by Cuhnt on 30/7/2019
 * Updated by Xiaro on 21/08/20
 */
@Module.Info(
        name = "Blink",
        category = Module.Category.PLAYER,
        description = "Cancels server side packets"
)
class Blink : Module() {
    private val autoReset = register(Settings.b("AutoReset", true))
    private val resetThreshold = register(Settings.integerBuilder("ResetThreshold").withValue(20).withRange(1, 100).withVisibility { autoReset.value }.build())

    private val packets: Queue<CPacketPlayer> = LinkedList()
    private var clonedPlayer: EntityOtherPlayerMP? = null
    private var sending = false

    @EventHandler
    private val listener = Listener(EventHook { event: PacketEvent.Send ->
        if (!sending && event.packet is CPacketPlayer) {
            event.cancel()
            packets.add(event.packet as CPacketPlayer)
        }
    })

    override fun onUpdate() {
        if (autoReset.value && packets.size >= resetThreshold.value) {
            end()
            begin()
        }
    }

    override fun onEnable() {
        begin()
    }

    override fun onDisable() {
        end()
    }

    private fun begin() {
        if (mc.player != null) {
            clonedPlayer = EntityOtherPlayerMP(mc.world, mc.getSession().profile)
            clonedPlayer!!.copyLocationAndAnglesFrom(mc.player)
            clonedPlayer!!.rotationYawHead = mc.player.rotationYawHead
            mc.world.addEntityToWorld(-100, clonedPlayer as Entity)
        }
    }

    private fun end() {
        sending = true
        while (packets.isNotEmpty()) mc.player.connection.sendPacket(packets.poll())
        val localPlayer: EntityPlayer? = mc.player
        if (localPlayer != null) {
            mc.world.removeEntityFromWorld(-100)
            clonedPlayer = null
        }
        sending = false
    }

    override fun getHudInfo(): String {
        return packets.size.toString()
    }
}