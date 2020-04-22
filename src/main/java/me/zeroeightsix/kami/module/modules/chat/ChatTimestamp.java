package me.zeroeightsix.kami.module.modules.chat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.ColourTextFormatting;
import me.zeroeightsix.kami.util.TimeUtil;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import static me.zeroeightsix.kami.util.ColourTextFormatting.toTextMap;

/**
 * @author dominikaaaa
 * Updated by dominikaaaa on 19/04/20
 */
@Module.Info(
        name = "ChatTimestamp",
        category = Module.Category.CHAT,
        description = "Shows the time a message was sent beside the message",
        showOnArray = Module.ShowOnArray.OFF
)
public class ChatTimestamp extends Module {
    private Setting<ColourTextFormatting.ColourCode> firstColour = register(Settings.e("First Colour", ColourTextFormatting.ColourCode.GRAY));
    private Setting<ColourTextFormatting.ColourCode> secondColour = register(Settings.e("Second Colour", ColourTextFormatting.ColourCode.GRAY));
    private Setting<TimeUtil.TimeType> timeTypeSetting = register(Settings.e("Time Format", TimeUtil.TimeType.HHMM));
    private Setting<TimeUtil.TimeUnit> timeUnitSetting = register(Settings.e("Time Unit", TimeUtil.TimeUnit.H24));
    private Setting<Boolean> doLocale = register(Settings.b("Show AMPM", true));

    @EventHandler
    public Listener<ClientChatReceivedEvent> listener = new Listener<>(event -> {
        if (mc.player == null) return;
        TextComponentString prefix = new TextComponentString(
                getFormattedTime()
        );
        event.setMessage(prefix.appendSibling(event.getMessage()));
    });

    public String getFormattedTime() {
        return "<" + TimeUtil.getFinalTime(setToText(secondColour.getValue()), setToText(firstColour.getValue()), timeUnitSetting.getValue(), timeTypeSetting.getValue(), doLocale.getValue()) + TextFormatting.RESET + "> ";
    }

    private TextFormatting setToText(ColourTextFormatting.ColourCode colourCode) {
        return toTextMap.get(colourCode);
    }
}