package me.zeroeightsix.kami.module.modules.combat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.Friends
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.CPacketUseEntity

@Module.Info(
        name = "AntiFriendHit",
        description = "Don't hit your friends",
        category = Module.Category.COMBAT
)
object AntiFriendHit : Module() {
    @EventHandler
    private val sendListener = Listener(EventHook { event: PacketEvent.Send ->
        if (event.packet !is CPacketUseEntity || event.packet.action != CPacketUseEntity.Action.ATTACK) return@EventHook
        val entity = mc.world?.let { event.packet.getEntityFromWorld(it) } ?: return@EventHook
        if (entity is EntityPlayer && Friends.isFriend(entity.name)) event.cancel()
    })
}