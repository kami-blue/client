package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import org.kamiblue.event.listener.listener
import net.minecraft.init.SoundEvents
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraft.util.SoundCategory

@Module.Info(
        name = "NoSoundLag",
        category = Module.Category.MISC,
        description = "Prevents lag caused by sound machines"
)
object NoSoundLag : Module() {
    init {
        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketSoundEffect) return@listener
            if (it.packet.getCategory() == SoundCategory.PLAYERS && it.packet.getSound() === SoundEvents.ITEM_ARMOR_EQUIP_GENERIC) {
                it.cancel()
            }
        }
    }
}