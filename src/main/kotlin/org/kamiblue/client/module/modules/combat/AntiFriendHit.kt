package org.kamiblue.client.module.modules.combat

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.CPacketUseEntity
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.manager.managers.FriendManager
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.event.listener.listener

internal object AntiFriendHit : Module(
    name = "AntiFriendHit",
    description = "Don't hit your friends",
    category = Category.COMBAT
) {
    init {
        listener<PacketEvent.Send> {
            if (it.packet !is CPacketUseEntity || it.packet.action != CPacketUseEntity.Action.ATTACK) return@listener
            val entity = mc.world?.let { world -> it.packet.getEntityFromWorld(world) } ?: return@listener
            if (entity is EntityPlayer && FriendManager.isFriend(entity.name)) {
                it.cancel()
            }
        }
    }
}