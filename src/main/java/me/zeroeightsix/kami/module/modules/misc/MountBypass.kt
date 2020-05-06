package me.zeroeightsix.kami.module.modules.misc

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import net.minecraft.entity.passive.AbstractChestHorse
import net.minecraft.network.play.client.CPacketUseEntity

/*
 * by ionar2
 */
@Module.Info(
        name = "MountBypass",
        category = Module.Category.MISC,
        description = "Might allow you to mount chested animals on servers that block it"
)
class MountBypass : Module() {
    @EventHandler
    private val onPacketEventSend = Listener(EventHook { event: PacketEvent.Send ->
        if (event.packet is CPacketUseEntity) {
            val packet = event.packet as CPacketUseEntity
            
            if (packet.getEntityFromWorld(mc.world) is AbstractChestHorse) {
                if (packet.action == CPacketUseEntity.Action.INTERACT_AT) {
                    event.cancel()
                }
            }
        }
    })
}