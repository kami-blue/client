package me.zeroeightsix.kami.module.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.network.play.client.*;

/**
 * @author dominikaaaa
 */
@Module.Info(
        name = "PacketCancel",
        description = "Cancels specific packets used for various actions",
        category = Module.Category.PLAYER
)
public class PacketCancel extends Module {
    private Setting<Boolean> all = register(Settings.b("All", false));
    private Setting<Boolean> packetInput = register(Settings.booleanBuilder("CPacketInput").withValue(true).withVisibility(v -> !all.getValue()));
    private Setting<Boolean> packetPlayer = register(Settings.booleanBuilder("CPacketPlayer").withValue(true).withVisibility(v -> !all.getValue()));
    private Setting<Boolean> packetEntityAction = register(Settings.booleanBuilder("CPacketEntityAction").withValue(true).withVisibility(v -> !all.getValue()));
    private Setting<Boolean> packetUseEntity = register(Settings.booleanBuilder("CPacketUseEntity").withValue(true).withVisibility(v -> !all.getValue()));
    private Setting<Boolean> packetVehicleMove = register(Settings.booleanBuilder("CPacketVehicleMove").withValue(true).withVisibility(v -> !all.getValue()));
    private int numPackets;
    
    @EventHandler
    private final Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
        if (
            (all.getValue())
            ||
            (packetInput.getValue() && event.getPacket() instanceof CPacketInput)
            ||
            (packetPlayer.getValue() && event.getPacket() instanceof CPacketPlayer)
            ||
            (packetEntityAction.getValue() && event.getPacket() instanceof CPacketEntityAction)
            ||
            (packetUseEntity.getValue() && event.getPacket() instanceof CPacketUseEntity)
            ||
            (packetVehicleMove.getValue() && event.getPacket() instanceof CPacketVehicleMove)
        ) {
            event.cancel();
            numPackets++;
        }
    });

    public void onDisable() {
        numPackets = 0;
    }

    @Override
    public String getHudInfo() {
        return Integer.toString(numPackets);
    }
}
