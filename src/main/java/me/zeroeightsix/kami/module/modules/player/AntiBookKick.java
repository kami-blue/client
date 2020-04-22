package me.zeroeightsix.kami.module.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.network.play.client.CPacketClickWindow;

import static me.zeroeightsix.kami.util.MessageSendHelper.sendWarningMessage;

/**
 * @author IronException
 * Used with permission from ForgeHax
 * https://github.com/fr1kin/ForgeHax/blob/bb522f8/src/main/java/com/matt/forgehax/mods/AntiBookKick.java
 * Permission (and ForgeHax is MIT licensed):
 * https://discordapp.com/channels/573954110454366214/634010802403409931/693919755647844352
 */
@Module.Info(
        name = "AntiBookKick",
        category = Module.Category.MISC,
        description = "Prevents being kicked by clicking on books",
        showOnArray = Module.ShowOnArray.OFF
)
public class AntiBookKick extends Module {
    @EventHandler
    public Listener<PacketEvent.Receive> listener = new Listener<>(event -> {
        if (!(event.getPacket() instanceof CPacketClickWindow)) return;
        final CPacketClickWindow packet = (CPacketClickWindow) event.getPacket();

        if (!(packet.getClickedItem().getItem() instanceof ItemWrittenBook)) return;

        event.cancel();
        sendWarningMessage(getChatName()
                + "Don't click the book \""
                + packet.getClickedItem().getDisplayName()
                + "\", shift click it instead!");

        mc.player.openContainer.slotClick(packet.getSlotId(), packet.getUsedButton(), packet.getClickType(), mc.player);
    });
}
