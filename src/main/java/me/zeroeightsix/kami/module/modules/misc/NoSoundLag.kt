package me.zeroeightsix.kami.module.modules.misc

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import net.minecraft.init.SoundEvents
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraft.util.SoundCategory

@Module.Info(
        name = "NoSoundLag",
        category = Module.Category.MISC,
        description = "Prevents lag caused by sound machines"
)
object NoSoundLag : Module() {
    @EventHandler
    private val receiveListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (mc.player == null || event.packet !is SPacketSoundEffect) return@EventHook
        if (event.packet.getCategory() == SoundCategory.PLAYERS && event.packet.getSound() === SoundEvents.ITEM_ARMOR_EQUIP_GENERIC) {
            event.cancel()
        }
    })
}