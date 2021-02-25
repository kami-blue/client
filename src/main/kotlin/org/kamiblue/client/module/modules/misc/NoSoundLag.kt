package org.kamiblue.client.module.modules.misc

import net.minecraft.init.SoundEvents
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraft.util.SoundCategory
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.event.listener.listener

internal object NoSoundLag : Module(
    name = "NoSoundLag",
    category = Category.MISC,
    description = "Prevents lag caused by sound machines"
) {
    init {
        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketSoundEffect) return@listener
            if (it.packet.category == SoundCategory.PLAYERS && it.packet.sound === SoundEvents.ITEM_ARMOR_EQUIP_GENERIC) {
                it.cancel()
            }
        }
    }
}