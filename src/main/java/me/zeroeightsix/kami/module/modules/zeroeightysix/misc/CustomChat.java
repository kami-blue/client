package me.zeroeightsix.kami.module.modules.zeroeightysix.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.network.play.client.CPacketChatMessage;

import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliiilllll.lllllllliiiliiilllll;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliiliiiii.lllllllliiiliiliiiii;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliiliiiil.lllllllliiiliiliiiil;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliiliiili.lllllllliiiliiliiili;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliiliiill.lllllllliiiliiliiill;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliiliilii.lllllllliiiliiliilii;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliiliilil.lllllllliiiliiliilil;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliiliilli.lllllllliiiliiliilli;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliiliilll.lllllllliiiliiliilll;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliililiii.lllllllliiiliililiii;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliililiil.lllllllliiiliililiil;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliililili.lllllllliiiliililili;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliililill.lllllllliiiliililill;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliilillii.lllllllliiiliilillii;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliilillil.lllllllliiiliilillil;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliilillli.lllllllliiiliilillli;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliilillll.lllllllliiiliilillll;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliilliiii.lllllllliiiliilliiii;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliilliiil.lllllllliiiliilliiil;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliilliili.lllllllliiiliilliili;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliilliill.lllllllliiiliilliill;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliillilii.lllllllliiiliillilii;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliillilil.lllllllliiiliillilil;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliillilli.lllllllliiiliillilli;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliillilll.lllllllliiiliillilll;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliillliii.lllllllliiiliillliii;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliillliil.lllllllliiiliillliil;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliilllili.lllllllliiiliilllili;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliilllill.lllllllliiiliilllill;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliillllii.lllllllliiiliillllii;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliillllil.lllllllliiiliillllil;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliillllli.lllllllliiiliillllli;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiiliillllll.lllllllliiiliillllll;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiililiiiiii.lllllllliiililiiiiii;
import static me.zeroeightsix.kami.module.modules.bewwawho.lllllllliiiliiilllli.lllllllliiililiiiiil.lllllllliiililiiiiil;

/**
 * Created by 086 on 8/04/2018.
 * Updated by S-B99 on 12/11/19
 */
@Module.Info(name = "CustomChat", category = Module.Category.MISC, description = "Chat ending. Now has modes!", showOnArray = Module.ShowOnArray.OFF)
public class CustomChat extends Module {

    public Setting<Boolean> startupGlobal = register(Settings.b("Enable Automatically", true));
    private Setting<TextMode> textMode = register(Settings.e("Text", TextMode.ONTOP));
    private Setting<DecoMode> decoMode = register(Settings.e("Decoration", DecoMode.NONE));
    private Setting<Boolean> commands = register(Settings.b("Commands", false));
    private Setting<Boolean> debug = register(Settings.b("Debug", true));


    private String kSep = " " + lllllllliiiliiilllll + " ";
    private String kClassic = " " + lllllllliiiliiliiiii + " ";
    private String kClassicO = " " + lllllllliiiliiliiili;
    private String kName = lllllllliiiliiliiiil + lllllllliiiliiliiill + lllllllliiiliiliilii + lllllllliiiliiliilli + " " + lllllllliiiliiliilil + lllllllliiiliiliilll + lllllllliiiliililiii + lllllllliiiliililiil;
    private String kOnTop = lllllllliiiliiliiiil + lllllllliiiliiliiill + lllllllliiiliiliilii + lllllllliiiliiliilli + " " + lllllllliiiliiliilil + lllllllliiiliiliilll + lllllllliiiliililiii + lllllllliiiliililiil + " " + lllllllliiiliililili + lllllllliiiliililill + " " + lllllllliiiliilillii + lllllllliiiliilillil + lllllllliiiliilillli;
    private String kWebsite = lllllllliiiliilillll + lllllllliiiliilliiii + lllllllliiiliilliiil + lllllllliiiliilliili + lllllllliiiliilliill + lllllllliiiliillilii + lllllllliiiliillilil + lllllllliiiliillilli + lllllllliiiliillilll + lllllllliiiliillliii + lllllllliiiliillliil + lllllllliiiliilllili + lllllllliiiliilllill + lllllllliiiliillllii + lllllllliiiliillllil + lllllllliiiliillllli + lllllllliiiliillllll + lllllllliiililiiiiii;
    private String kfinal = "";
    private String kAll = lllllllliiililiiiiil;

    @Override
    public void onEnable() {
        if (mc.player != null && debug.getValue()) {
            Command.sendChatMessage("[CustomChat] Note: ALL text mode only works with the separator decoration mode");
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
            }
            if (decoMode.getValue().equals(DecoMode.SEPARATOR)) {
                if (textMode.getValue().equals(TextMode.NAME)) {
                    kfinal = kSep + kName;
                } else if (textMode.getValue().equals(TextMode.ONTOP)) {
                    kfinal = kSep + kOnTop;
                } else if (textMode.getValue().equals(TextMode.WEBSITE)) {
                    kfinal = kSep + kWebsite;
                } else if (textMode.getValue().equals(TextMode.ALL)) {
                    kfinal = kAll;
                }
            } else if (decoMode.getValue().equals(DecoMode.NONE)) {
                if (textMode.getValue().equals(TextMode.NAME)) {
                    kfinal = " " + kName;
                } else if (textMode.getValue().equals(TextMode.ONTOP)) {
                    kfinal = " " + kOnTop;
                } else if (textMode.getValue().equals(TextMode.WEBSITE)) {
                    kfinal = " " + kWebsite;
                }
            } else if (decoMode.getValue().equals(DecoMode.CLASSIC)) {
                if (textMode.getValue().equals(TextMode.NAME)) {
                    kfinal = kClassic + kName + kClassicO;
                } else if (textMode.getValue().equals(TextMode.ONTOP)) {
                    kfinal = kClassic + kOnTop + kClassicO;
                } else if (textMode.getValue().equals(TextMode.WEBSITE)) {
                    kfinal = kClassic + kWebsite + kClassicO;
                }
            }
            s += kfinal;
            if (s.length() >= 256) s = s.substring(0, 256);
            ((CPacketChatMessage) event.getPacket()).message = s;
        }
    });

    private enum TextMode {
        NAME, ONTOP, WEBSITE, ALL
    }

    private enum DecoMode {
        SEPARATOR, CLASSIC, NONE

    }

}
