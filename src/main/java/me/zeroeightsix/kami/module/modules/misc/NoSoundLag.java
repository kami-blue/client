package me.zeroeightsix.kami.module.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundCategory;

/**
 * Created by cats on 1/12/2019
 */

@Module.Info(
        name = "NoSoundLag",
        category = Module.Category.MISC,
        description = "Prevents lag caused by sound machines"
)
public class NoSoundLag extends Module {

    @EventHandler
    Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
        if (mc.player == null) return;
        if (event.getPacket() instanceof SPacketSoundEffect) {
            final SPacketSoundEffect soundPacket = (SPacketSoundEffect) event.getPacket();
            if (soundPacket.getCategory() == SoundCategory.PLAYERS && soundPacket.getSound() == SoundEvents.ITEM_ARMOR_EQUIP_GENERIC) {
                event.cancel();
            }
        }
    });

}
