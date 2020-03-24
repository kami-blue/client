package me.zeroeightsix.kami.module.modules.chat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.modules.gui.InfoOverlay;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.ColourUtils;
import me.zeroeightsix.kami.util.TimeUtil;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.TextFormatting;

/**
 * @author S-B99
 * Updated by S-B99 on 06/02/20
 */
@Module.Info(name = "ChatTimestamp", category = Module.Category.CHAT, description = "Shows the time a message was sent beside the message", showOnArray = Module.ShowOnArray.OFF)
public class ChatTimestamp extends Module {
    private Setting<TextFormatting> firstColour = register(Settings.e("First Colour", TextFormatting.valueOf("GRAY")));
    private Setting<TextFormatting> secondColour = register(Settings.e("Second Colour", TextFormatting.valueOf("WHITE")));
    private Setting<TimeUtil.TimeType> timeTypeSetting = register(Settings.e("Time Format", TimeUtil.TimeType.HHMM));
    private Setting<TimeUtil.TimeUnit> timeUnitSetting = register(Settings.e("Time Unit", TimeUtil.TimeUnit.H12));
    private Setting<Boolean> doLocale = register(Settings.b("Show AMPM", true));

    @EventHandler
    public Listener<PacketEvent.Receive> listener = new Listener<>(event -> {
        if (mc.player == null || isDisabled()) return;

        if (!(event.getPacket() instanceof SPacketChat)) return;
        SPacketChat sPacketChat = (SPacketChat) event.getPacket();

        if (addTime(sPacketChat.getChatComponent().getUnformattedText())) {
            event.cancel();
        }
    });

    private boolean addTime(String message) {
        Command.sendRawChatMessage("<" + TimeUtil.getFinalTime(secondColour.getValue(), firstColour.getValue(), timeUnitSetting.getValue(), timeTypeSetting.getValue(), doLocale.getValue()) + TextFormatting.RESET + "> " + message);
        return true;
    }
}