package me.zeroeightsix.kami.module.modules.gui;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.ColourTextFormatting;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static me.zeroeightsix.kami.command.Command.sendDisableMessage;
import static me.zeroeightsix.kami.util.ColourConverter.rgbToInt;
import static me.zeroeightsix.kami.util.ColourTextFormatting.colourEnumMap;
import static me.zeroeightsix.kami.util.ColourTextFormatting.toTextMap;
import static me.zeroeightsix.kami.util.InfoCalculator.isNumberEven;
import static me.zeroeightsix.kami.util.InfoCalculator.reverseNumber;

/**
 * @author S-B99
 * Created by S-B99 on 20/03/20
 * Updated by S-B99 on 25/03/20
 */
@Module.Info(name = "ActiveModules", category = Module.Category.CLIENT, description = "Configures ActiveModules Colour", showOnArray = Module.ShowOnArray.OFF)
public class ActiveModules extends Module {
    private Setting<Boolean> forgeHax = register(Settings.b("ForgeHax", false));
    public Setting<Mode> mode = register(Settings.e("Mode", Mode.RAINBOW));
    private Setting<Integer> rainbowSpeed = register(Settings.integerBuilder().withName("Speed R").withValue(30).withMinimum(0).withMaximum(100).withVisibility(v -> mode.getValue().equals(Mode.RAINBOW)).build());
    public Setting<Integer> saturationR = register(Settings.integerBuilder().withName("Saturation R").withValue(117).withMinimum(0).withMaximum(255).withVisibility(v -> mode.getValue().equals(Mode.RAINBOW)).build());
    public Setting<Integer> brightnessR = register(Settings.integerBuilder().withName("Brightness R").withValue(255).withMinimum(0).withMaximum(255).withVisibility(v -> mode.getValue().equals(Mode.RAINBOW)).build());
    public Setting<Integer> hueC = register(Settings.integerBuilder().withName("Hue C").withValue(178).withMinimum(0).withMaximum(255).withVisibility(v -> mode.getValue().equals(Mode.CUSTOM)).build());
    public Setting<Integer> saturationC = register(Settings.integerBuilder().withName("Saturation C").withValue(156).withMinimum(0).withMaximum(255).withVisibility(v -> mode.getValue().equals(Mode.CUSTOM)).build());
    public Setting<Integer> brightnessC = register(Settings.integerBuilder().withName("Brightness C").withValue(255).withMinimum(0).withMaximum(255).withVisibility(v -> mode.getValue().equals(Mode.CUSTOM)).build());
    private Setting<Boolean> alternate = register(Settings.booleanBuilder().withName("Alternate").withValue(true).withVisibility(v -> mode.getValue().equals(Mode.INFO_OVERLAY)).build());

    public static int getCategoryColour(Module module) {
        switch (module.getCategory()) {
            case CHAT: return rgbToInt(129, 171, 174);
            case COMBAT: return rgbToInt(162, 25, 14);
            case EXPERIMENTAL: return rgbToInt(175, 175, 31);
            case CLIENT: return rgbToInt(158, 159, 197);
            case RENDER: return rgbToInt(51, 197, 130);
            case PLAYER: return rgbToInt(99, 202, 191);
            case MOVEMENT: return rgbToInt(7, 77, 227);
            case MISC: return rgbToInt(247, 215, 59);
            case UTILS: return rgbToInt(46, 212, 77);
            default: return rgbToInt(139, 100, 255);
        }
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

    public int getRainbowSpeed() {
        int rSpeed = reverseNumber(rainbowSpeed.getValue(), 1, 100);
        if (rSpeed == 0) return 1; // can't divide by 0
        else return rSpeed;
    }

    public String fHax() {
        if (forgeHax.getValue()) return ">";
        else return "";
    }

    public enum Mode { RAINBOW, CUSTOM, CATEGORY, INFO_OVERLAY }
    public void onDisable() { sendDisableMessage(this.getClass()); }
}
