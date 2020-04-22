package me.zeroeightsix.kami.module.modules.misc;

import io.netty.buffer.Unpooled;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;

/**
 * Created by 0x2E | PretendingToCode
 */
@Module.Info(
        name = "BeaconSelector",
        category = Module.Category.MISC,
        description = "Choose any of the 5 beacon effects regardless of beacon base height"
)
public class BeaconSelector extends Module {
    public static int effect = -1;
    private boolean doCancelPacket = true;

    @EventHandler
    public Listener<PacketEvent.Send> packetListener = new Listener<>(event -> {
        if (event.getPacket() instanceof CPacketCustomPayload && ((CPacketCustomPayload) event.getPacket()).getChannelName().equals("MC|Beacon") && doCancelPacket) {
            doCancelPacket = false;

            PacketBuffer data = ((CPacketCustomPayload) event.getPacket()).getBufferData();
            /* i1 is actually not unused, reading the int discards the bytes it read, allowing k1 to read the next bytes */
            @SuppressWarnings("unused")
            int i1 = data.readInt(); // primary
            int k1 = data.readInt(); // secondary

            event.cancel();

            PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
            buf.writeInt(effect);
            buf.writeInt(k1);

            Wrapper.getPlayer().connection.sendPacket(new CPacketCustomPayload("MC|Beacon", buf));

            doCancelPacket = true;
        }
    });
}
