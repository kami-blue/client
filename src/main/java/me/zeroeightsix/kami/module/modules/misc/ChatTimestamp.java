package me.zeroeightsix.kami.module.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.ColourUtils;
import me.zeroeightsix.kami.util.TimeUtil;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.TextFormatting;

/**
 * @author S-B99
 * Updated by S-B99 on 28/01/20
 */
@Module.Info(name = "ChatTimestamp", category = Module.Category.MISC)
public class ChatTimestamp extends Module {
    private Setting<ColourUtils.ColourCode> firstColour = register(Settings.e("First Colour", ColourUtils.ColourCode.GREY));
    private Setting<ColourUtils.ColourCode> secondColour = register(Settings.e("Second Colour", ColourUtils.ColourCode.WHITE));
    private Setting<TimeUtil.TimeType> timeTypeSetting = register(Settings.e("Time Format", TimeUtil.TimeType.HHMM));
    private Setting<TimeUtil.TimeUnit> timeUnitSetting = register(Settings.e("Time Unit", TimeUtil.TimeUnit.h12));

    @EventHandler
    public Listener<PacketEvent.Receive> listener = new Listener<>(event -> {
        if (mc.player == null || this.isDisabled()) return;

        if (!(event.getPacket() instanceof SPacketChat)) return;
        SPacketChat sPacketChat = (SPacketChat) event.getPacket();

        if (addTime(sPacketChat.getChatComponent().getUnformattedText())) {
            event.cancel();
        }
    });

    private boolean addTime(String message) {
        Command.sendRawChatMessage("<" + TimeUtil.getFinalTime(secondColour.getValue(), firstColour.getValue(), timeUnitSetting.getValue(), timeTypeSetting.getValue()) + TextFormatting.RESET + "> " + message);
        return true;
    }


}