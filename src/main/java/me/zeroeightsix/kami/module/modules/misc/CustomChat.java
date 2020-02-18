package me.zeroeightsix.kami.module.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.network.play.client.CPacketChatMessage;

import static me.zeroeightsix.kami.KamiMod.*;

/**
 * Created by 086 on 8/04/2018.
 * Updated by S-B99 on 04/02/20
 */
@Module.Info(name = "CustomChat", category = Module.Category.MISC, description = "Adds a watermark to the end of your message to let others know you're using KAMI Blue", showOnArray = Module.ShowOnArray.OFF)
public class CustomChat extends Module {

    public Setting<Boolean> startupGlobal = register(Settings.b("Enable Automatically", true));
    public Setting<TextMode> textMode = register(Settings.e("Message", TextMode.ONTOP));
    private Setting<DecoMode> decoMode = register(Settings.e("Separator", DecoMode.NONE));
    private Setting<Boolean> commands = register(Settings.b("Commands", false));
    public Setting<String> customText = register(Settings.stringBuilder("Custom Text").withValue("Use &7" + Command.getCommandPrefix() + "&rcustomchat to modify this").withConsumer((old, value) -> {}));

    public enum TextMode {
        NAME, ONTOP, WEBSITE, JAPANESE, CUSTOM
    }

    private enum DecoMode {
        SEPARATOR, CLASSIC, NONE
    }

    private String getText(TextMode t) {
        switch (t) {
            case NAME: return KAMI_BLUE;
            case ONTOP: return KAMI_ONTOP;
            case WEBSITE: return KAMI_WEBSITE;
            case JAPANESE: return KAMI_JAPANESE_ONTOP;
            case CUSTOM: return customText.getValue();
            default: return "";
        }
    }

    private String getFull(DecoMode d) {
        switch (d) {
            case NONE: return " " + getText(textMode.getValue());
            case CLASSIC: return  " " + quoteLeft + " " + getText(textMode.getValue()) + " " + quoteRight;
            case SEPARATOR: return " " + separator + " " + getText(textMode.getValue());
            default: return "";
        }
    }

    @EventHandler
    public Listener<PacketEvent.Send> listener = new Listener<>(event -> {
        if (event.getPacket() instanceof CPacketChatMessage) {
            String s = ((CPacketChatMessage) event.getPacket()).getMessage();
            if (!commands.getValue()) {
                if (s.startsWith("/")) return;
                else if (s.startsWith(",")) return;
                else if (s.startsWith(".")) return;
                else if (s.startsWith("-")) return;
                else if (s.startsWith(";")) return;
                else if (s.startsWith("?")) return;
                else if (s.startsWith("*")) return;
                else if (s.startsWith("^")) return;
                else if (s.startsWith("&")) return;
            }
            s += getFull(decoMode.getValue());
            if (s.length() >= 256) s = s.substring(0, 256);
            ((CPacketChatMessage) event.getPacket()).message = s;
        }
    });

}
