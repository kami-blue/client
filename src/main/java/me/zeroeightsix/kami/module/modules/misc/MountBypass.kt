package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import net.minecraft.entity.passive.AbstractChestHorse
import net.minecraft.network.play.client.CPacketUseEntity
import org.kamiblue.event.listener.listener

internal object MountBypass : Module(
    name = "MountBypass",
    category = Category.MISC,
    description = "Might allow you to mount chested animals on servers that block it"
) {
    init {
        listener<PacketEvent.Send> {
            if (it.packet !is CPacketUseEntity || it.packet.action != CPacketUseEntity.Action.INTERACT_AT) return@listener
            if (it.packet.getEntityFromWorld(mc.world) is AbstractChestHorse) it.cancel()
        }
    }
}