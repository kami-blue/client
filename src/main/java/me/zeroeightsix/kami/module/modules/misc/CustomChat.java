package me.zeroeightsix.kami.module.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.network.play.client.CPacketChatMessage;

import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliiilllll.lllllllliiiliiilllll;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliiliiiii.lllllllliiiliiliiiii;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliiliiiil.lllllllliiiliiliiiil;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliiliiili.lllllllliiiliiliiili;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliiliiill.lllllllliiiliiliiill;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliiliilii.lllllllliiiliiliilii;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliiliilil.lllllllliiiliiliilil;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliiliilli.lllllllliiiliiliilli;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliiliilll.lllllllliiiliiliilll;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliililiii.lllllllliiiliililiii;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliililiil.lllllllliiiliililiil;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliililili.lllllllliiiliililili;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliililill.lllllllliiiliililill;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliilillii.lllllllliiiliilillii;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliilillil.lllllllliiiliilillil;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliilillli.lllllllliiiliilillli;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliilillll.lllllllliiiliilillll;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliilliiii.lllllllliiiliilliiii;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliilliiil.lllllllliiiliilliiil;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliilliili.lllllllliiiliilliili;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliilliill.lllllllliiiliilliill;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliillilii.lllllllliiiliillilii;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliillilil.lllllllliiiliillilil;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliillilli.lllllllliiiliillilli;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliillilll.lllllllliiiliillilll;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliillliii.lllllllliiiliillliii;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliillliil.lllllllliiiliillliil;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliilllili.lllllllliiiliilllili;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliilllill.lllllllliiiliilllill;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliillllii.lllllllliiiliillllii;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliillllil.lllllllliiiliillllil;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliillllli.lllllllliiiliillllli;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiiliillllll.lllllllliiiliillllll;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiililiiiiii.lllllllliiililiiiiii;
import static me.zeroeightsix.kami.module.modules.sdashb.lllllllliiiliiilllli.lllllllliiililiiiiil.lllllllliiililiiiiil;

/**
 * Created by 086 on 8/04/2018.
 * Updated by S-B99 on 12/11/19
 */
@Module.Info(name = "CustomChat", category = Module.Category.MISC, description = "Chat ending. Now has modes!")
public class CustomChat extends Module {

    private Setting<TextMode> textMode = register(Settings.e("Text", TextMode.WEBSITE));
    private Setting<DecoMode> decoMode = register(Settings.e("Decoration", DecoMode.CLASSIC));
    private Setting<Boolean> commands = register(Settings.b("Commands", false));
    private Setting<Boolean> debug = register(Settings.b("Debug", true));


    private String KAMI_SEPARATOR = " " + lllllllliiiliiilllll + " ";
    private String KAMI_CLASSIC = " " + lllllllliiiliiliiiii + " ";
    private String KAMI_CLASSIC_OTHER = " " + lllllllliiiliiliiili;
    private String KAMI_NAME = lllllllliiiliiliiiil + lllllllliiiliiliiill + lllllllliiiliiliilii + lllllllliiiliiliilli + " " + lllllllliiiliiliilil + lllllllliiiliiliilll + lllllllliiiliililiii + lllllllliiiliililiil;
    private String KAMI_ONTOP = lllllllliiiliiliiiil + lllllllliiiliiliiill + lllllllliiiliiliilii + lllllllliiiliiliilli + " " + lllllllliiiliiliilil + lllllllliiiliiliilll + lllllllliiiliililiii + lllllllliiiliililiil + " " + lllllllliiiliililili + lllllllliiiliililill + " " + lllllllliiiliilillii + lllllllliiiliilillil + lllllllliiiliilillli;
    private String KAMI_WEBSITE = lllllllliiiliilillll + lllllllliiiliilliiii + lllllllliiiliilliiil + lllllllliiiliilliili + lllllllliiiliilliill + lllllllliiiliillilii + lllllllliiiliillilil + lllllllliiiliillilli + lllllllliiiliillilll + lllllllliiiliillliii + lllllllliiiliillliil + lllllllliiiliilllili + lllllllliiiliilllill + lllllllliiiliillllii + lllllllliiiliillllil + lllllllliiiliillllli + lllllllliiiliillllll + lllllllliiililiiiiii;
    private String KAMI_FINAL = "";
    private String KAMI_ALL = lllllllliiililiiiiil;

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
                    KAMI_FINAL = KAMI_SEPARATOR + KAMI_NAME;
                }
                else if (textMode.getValue().equals(TextMode.ONTOP)) {
                    KAMI_FINAL = KAMI_SEPARATOR + KAMI_ONTOP;
                }
                else if (textMode.getValue().equals(TextMode.WEBSITE)) {
                    KAMI_FINAL = KAMI_SEPARATOR + KAMI_WEBSITE;
                }
                else if (textMode.getValue().equals(TextMode.ALL)) {
                    KAMI_FINAL = KAMI_ALL;
                }
            }
            else if (decoMode.getValue().equals(DecoMode.NONE)) {
                if (textMode.getValue().equals(TextMode.NAME)) {
                    KAMI_FINAL = " " + KAMI_NAME;
                }
                else if (textMode.getValue().equals(TextMode.ONTOP)) {
                    KAMI_FINAL = " " + KAMI_ONTOP;
                }
                else if (textMode.getValue().equals(TextMode.WEBSITE)) {
                    KAMI_FINAL = " " + KAMI_WEBSITE;
                }
            }
            else if (decoMode.getValue().equals(DecoMode.CLASSIC)) {
                if (textMode.getValue().equals(TextMode.NAME)) {
                    KAMI_FINAL = KAMI_CLASSIC + KAMI_NAME + KAMI_CLASSIC_OTHER;
                }
                else if (textMode.getValue().equals(TextMode.ONTOP)) {
                    KAMI_FINAL = KAMI_CLASSIC + KAMI_ONTOP + KAMI_CLASSIC_OTHER;
                }
                else if (textMode.getValue().equals(TextMode.WEBSITE)) {
                    KAMI_FINAL = KAMI_CLASSIC + KAMI_WEBSITE + KAMI_CLASSIC_OTHER;
                }
            }
                s += KAMI_FINAL;
            if (s.length() >= 256) s = s.substring(0,256);
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
