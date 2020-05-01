package me.zeroeightsix.kami.module.modules.client;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.ColourTextFormatting;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static me.zeroeightsix.kami.util.ColourConverter.rgbToInt;
import static me.zeroeightsix.kami.util.ColourTextFormatting.colourEnumMap;
import static me.zeroeightsix.kami.util.ColourTextFormatting.toTextMap;
import static me.zeroeightsix.kami.util.MathsUtils.isNumberEven;
import static me.zeroeightsix.kami.util.MathsUtils.reverseNumber;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendDisableMessage;

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 20/03/20
 * Updated by dominikaaaa on 04/04/20
 */
@Module.Info(
        name = "ActiveModules",
        category = Module.Category.CLIENT,
        description = "Configures ActiveModules colours and modes",
        showOnArray = Module.ShowOnArray.OFF
)
public class ActiveModules extends Module {
    private Setting<Boolean> forgeHax = register(Settings.b("ForgeHax", false));
    public Setting<Boolean> potion = register(Settings.b("Potions Move", false));
    public Setting<Mode> mode = register(Settings.e("Mode", Mode.RAINBOW));
    private Setting<Integer> rainbowSpeed = register(Settings.integerBuilder().withName("Speed R").withValue(30).withMinimum(0).withMaximum(100).withVisibility(v -> mode.getValue().equals(Mode.RAINBOW)).build());
    public Setting<Integer> saturationR = register(Settings.integerBuilder().withName("Saturation R").withValue(117).withMinimum(0).withMaximum(255).withVisibility(v -> mode.getValue().equals(Mode.RAINBOW)).build());
    public Setting<Integer> brightnessR = register(Settings.integerBuilder().withName("Brightness R").withValue(255).withMinimum(0).withMaximum(255).withVisibility(v -> mode.getValue().equals(Mode.RAINBOW)).build());
    public Setting<Integer> hueC = register(Settings.integerBuilder().withName("Hue C").withValue(178).withMinimum(0).withMaximum(255).withVisibility(v -> mode.getValue().equals(Mode.CUSTOM)).build());
    public Setting<Integer> saturationC = register(Settings.integerBuilder().withName("Saturation C").withValue(156).withMinimum(0).withMaximum(255).withVisibility(v -> mode.getValue().equals(Mode.CUSTOM)).build());
    public Setting<Integer> brightnessC = register(Settings.integerBuilder().withName("Brightness C").withValue(255).withMinimum(0).withMaximum(255).withVisibility(v -> mode.getValue().equals(Mode.CUSTOM)).build());
    private Setting<Boolean> alternate = register(Settings.booleanBuilder().withName("Alternate").withValue(true).withVisibility(v -> mode.getValue().equals(Mode.INFO_OVERLAY)).build());

    public Setting<String> chat = register(Settings.s("Chat", "162,136,227"));
    public Setting<String> combat = register(Settings.s("Combat", "229,68,109"));
    public Setting<String> experimental = register(Settings.s("Experimental", "211,188,192"));
    public Setting<String> client = register(Settings.s("Client", "56,2,59"));
    public Setting<String> render = register(Settings.s("Render", "105,48,109"));
    public Setting<String> player = register(Settings.s("Player", "255,137,102"));
    public Setting<String> movement = register(Settings.s("Movement", "111,60,145"));
    public Setting<String> misc = register(Settings.s("Misc", "165,102,139"));

    private static int getRgb(String input, int arrayPos) {
        String[] toConvert = input.split(",");
        return Integer.parseInt(toConvert[arrayPos]);
    }

    public int getInfoColour(int position) {
        if (!alternate.getValue()) return settingsToColour(false);
        else {
            if (isNumberEven(position)) {
                return settingsToColour(true);
            } else {
                return settingsToColour(false);
            }
        }
    }

    private int settingsToColour(boolean isOne) {
        Color localColor;
        switch (infoGetSetting(isOne)) {
            case UNDERLINE:
            case ITALIC:
            case RESET:
            case STRIKETHROUGH:
            case OBFUSCATED:
            case BOLD:
                localColor = colourEnumMap.get(TextFormatting.WHITE).colorLocal; break;
            default:
                localColor = colourEnumMap.get(infoGetSetting(isOne)).colorLocal;
        }
        return rgbToInt(localColor.getRed(), localColor.getGreen(), localColor.getBlue());
    }

    private TextFormatting infoGetSetting(boolean isOne) {
        InfoOverlay infoOverlay = (InfoOverlay) MODULE_MANAGER.getModule(InfoOverlay.class);
        if (isOne) return setToText(infoOverlay.firstColour.getValue());
        else return setToText(infoOverlay.secondColour.getValue());

    }

    private TextFormatting setToText(ColourTextFormatting.ColourCode colourCode) {
        return toTextMap.get(colourCode);
    }

    public int getCategoryColour(Module module) {
        switch (module.getCategory()) {
            case CHAT: return rgbToInt(getRgb(chat.getValue(), 0), getRgb(chat.getValue(), 1), getRgb(chat.getValue(), 2));
            case COMBAT: return rgbToInt(getRgb(combat.getValue(), 0), getRgb(combat.getValue(), 1), getRgb(combat.getValue(), 2));
            case EXPERIMENTAL: return rgbToInt(getRgb(experimental.getValue(), 0), getRgb(experimental.getValue(), 1), getRgb(experimental.getValue(), 2));
            case CLIENT: return rgbToInt(getRgb(client.getValue(), 0), getRgb(client.getValue(), 1), getRgb(client.getValue(), 2));
            case RENDER: return rgbToInt(getRgb(render.getValue(), 0), getRgb(render.getValue(), 1), getRgb(render.getValue(), 2));
            case PLAYER: return rgbToInt(getRgb(player.getValue(), 0), getRgb(player.getValue(), 1), getRgb(player.getValue(), 2));
            case MOVEMENT: return rgbToInt(getRgb(movement.getValue(), 0), getRgb(movement.getValue(), 1), getRgb(movement.getValue(), 2));
            case MISC: return rgbToInt(getRgb(misc.getValue(), 0), getRgb(misc.getValue(), 1), getRgb(misc.getValue(), 2));
            default: return rgbToInt(1, 1, 1);
        }
    }

    public int getRainbowSpeed() {
        int rSpeed = reverseNumber(rainbowSpeed.getValue(), 1, 100);
        if (rSpeed == 0) return 1; // can't divide by 0
        else return rSpeed;
    }

    public String getAlignedText(String name, String hudInfo, boolean right) {
        String aligned;
        if (right) {
            aligned = hudInfo + " " + name;
        } else {
            aligned = name + " " + hudInfo;
        }

        if (!forgeHax.getValue()) {
            return aligned;
        } else if (right) {
            return aligned + "<";
        } else {
            return ">" + aligned;
        }
    }

    public enum Mode { RAINBOW, CUSTOM, CATEGORY, INFO_OVERLAY }
    public void onDisable() { sendDisableMessage(this.getClass()); }
}
