package org.kamiblue.client.module.modules.player

import net.minecraft.network.play.server.SPacketPlayerPosLook
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.mixin.extension.rotationPitch
import org.kamiblue.client.mixin.extension.rotationYaw
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.event.listener.listener

internal object AntiForceLook : Module(
    name = "AntiForceLook",
    category = Category.PLAYER,
    description = "Stops server packets from turning your head"
) {
    init {
        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketPlayerPosLook || mc.player == null) return@listener
            it.packet.rotationYaw = mc.player.rotationYaw
            it.packet.rotationPitch = mc.player.rotationPitch
        }
    }
}