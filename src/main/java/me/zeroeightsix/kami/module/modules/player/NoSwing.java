package me.zeroeightsix.kami.module.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import net.minecraft.network.play.client.CPacketAnimation;

/**
 * Created 13 August 2019 by hub
 * Updated 14 November 2019 by hub
 * Updated by dominikaaaa on 14/12/19
 */

@Module.Info(
        name = "NoSwing",
        category = Module.Category.PLAYER,
        description = "Cancels server and client swinging packets"
)
public class NoSwing extends Module {

    @EventHandler
    private Listener<PacketEvent.Send> listener = new Listener<>(event -> {
        if (event.getPacket() instanceof CPacketAnimation) {
            event.cancel();
        }
    });

}
