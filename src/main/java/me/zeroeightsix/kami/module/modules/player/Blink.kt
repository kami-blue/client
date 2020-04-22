package me.zeroeightsix.kami.module.modules.player

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.CPacketPlayer
import java.util.*

/**
 * Created by 086 on 24/01/2018.
 * Edited by Cuhnt on 30/7/2019
 */
@Module.Info(
        name = "Blink",
        category = Module.Category.PLAYER,
        description = "Cancels server side packets"
)
class Blink : Module() {
    private var packets: Queue<CPacketPlayer> = LinkedList()

    @EventHandler
    var listener = Listener(EventHook { event: PacketEvent.Send ->
        if (isEnabled && event.packet is CPacketPlayer) {
            event.cancel()
            packets.add(event.packet as CPacketPlayer)
        }
    })
    private var clonedPlayer: EntityOtherPlayerMP? = null
    override fun onEnable() {
        if (mc.player != null) {
            clonedPlayer = EntityOtherPlayerMP(mc.world, mc.getSession().profile)
            clonedPlayer!!.copyLocationAndAnglesFrom(mc.player)
            clonedPlayer!!.rotationYawHead = mc.player.rotationYawHead
            mc.world.addEntityToWorld(-100, clonedPlayer)
        }
    }

    override fun onDisable() {
        while (!packets.isEmpty()) mc.player.connection.sendPacket(packets.poll())
        val localPlayer: EntityPlayer? = mc.player
        if (localPlayer != null) {
            mc.world.removeEntityFromWorld(-100)
            clonedPlayer = null
        }
    }

    override fun getHudInfo(): String {
        return packets.size.toString()
    }
}