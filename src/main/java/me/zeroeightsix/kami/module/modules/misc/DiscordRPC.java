package me.zeroeightsix.kami.module.modules.misc;

import me.zeroeightsix.kami.DiscordPresence;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

import static me.zeroeightsix.kami.util.InfoCalculator.playerDimension;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendWarningMessage;

/**
 * @author dominikaaaa
 * Updated by dominikaaaa on 13/01/20
 * Updated (slightly) by Dewy on 3rd April 2020
 */
@Module.Info(
        name = "DiscordRPC",
        category = Module.Category.MISC,
        description = "Discord Rich Presence"
)
public class DiscordRPC extends Module {
    private Setting<Boolean> coordsConfirm = register(Settings.b("Coords Confirm", false));
    public Setting<LineInfo> line1Setting = register(Settings.e("Line 1 Left", LineInfo.VERSION)); // details left
    public Setting<LineInfo> line3Setting = register(Settings.e("Line 1 Right", LineInfo.USERNAME)); // details right
    public Setting<LineInfo> line2Setting = register(Settings.e("Line 2 Left", LineInfo.SERVER_IP)); // state left
    public Setting<LineInfo> line4Setting = register(Settings.e("Line 2 Right", LineInfo.HEALTH)); // state right

    public enum LineInfo {
        VERSION, WORLD, DIMENSION, USERNAME, HEALTH, SERVER_IP, COORDS, NONE
    }

    public String getLine(LineInfo line) {
        switch (line) {
            case VERSION: return KamiMod.MODVERSMALL;
            case WORLD:
                if (mc.isIntegratedServerRunning()) return "Singleplayer";
                else if (mc.getCurrentServerData() != null) return "Multiplayer";
                else return "Main Menu";
            case DIMENSION:
                return playerDimension(mc);
            case USERNAME:
                if (mc.player != null) return mc.player.getName();
                else return mc.getSession().getUsername();
            case HEALTH:
                if (mc.player != null) return ((int) mc.player.getHealth()) + " hp";
                else return "No hp";
            case SERVER_IP:
                if (mc.getCurrentServerData() != null) return mc.getCurrentServerData().serverIP;
                else if (mc.isIntegratedServerRunning()) return "Offline";
                else return "Main Menu";
            case COORDS:
                if (mc.player != null && coordsConfirm.getValue()) return "(" + (int) mc.player.posX + " " + (int) mc.player.posY + " " + (int) mc.player.posZ + ")";
                else return "No coords";
            default: return "";
        }
    }

    @Override
    public void onEnable() {
        DiscordPresence.start();
    }

    private static long startTime = 0;
    @Override
    public void onUpdate() {
        if (startTime == 0) startTime = System.currentTimeMillis();
        if (startTime + 10000 <= System.currentTimeMillis()) { // 10 seconds in milliseconds
            if (line1Setting.getValue().equals(LineInfo.COORDS) || line2Setting.getValue().equals(LineInfo.COORDS) || line3Setting.getValue().equals(LineInfo.COORDS) || line4Setting.getValue().equals(LineInfo.COORDS)) {
                if (!coordsConfirm.getValue() && mc.player != null) {
                    sendWarningMessage(getChatName() + " Warning: In order to use the coords option please enable the coords confirmation option. This will display your coords on the discord rpc. Do NOT use this if you do not want your coords displayed");
                }
            }
            startTime = System.currentTimeMillis();
        }
    }

    @Override
    protected void onDisable() {
        DiscordPresence.end();
    }
}
