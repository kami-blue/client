package org.kamiblue.client.module.modules.player

import net.minecraft.network.play.client.CPacketCloseWindow
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.mixin.extension.windowID
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.event.listener.listener

internal object XCarry : Module(
    name = "XCarry",
    category = Category.PLAYER,
    description = "Store items in crafting slots"
) {
    init {
        listener<PacketEvent.Send> {
            if (it.packet is CPacketCloseWindow && it.packet.windowID == 0) {
                it.cancel()
            }
        }
    }
}