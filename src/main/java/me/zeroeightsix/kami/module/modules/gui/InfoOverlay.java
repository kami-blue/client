package me.zeroeightsix.kami.module.modules.gui;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.modules.movement.TimerSpeed;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.InfoCalculator;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * @author S-B99
 * Created by S-B99 on 04/12/19
 * Updated by S-B99 on 28/01/20
 */
@Module.Info(name = "InfoOverlay", category = Module.Category.GUI, description = "Configures game information overlay", showOnArray = Module.ShowOnArray.OFF)
public class InfoOverlay extends Module {

    public Setting<Boolean> version = register(Settings.b("Version", true));
    public Setting<Boolean> username = register(Settings.b("Username", true));
    public Setting<Boolean> time = register(Settings.b("Time", true));
    public Setting<Boolean> tps = register(Settings.b("Ticks Per Second", false));
    public Setting<Boolean> fps = register(Settings.b("Frames Per Second", true));
    public Setting<Boolean> speed = register(Settings.b("Speed", true));
    public Setting<Boolean> timerSpeed = register(Settings.b("Timer Speed", false));
    public Setting<Boolean> ping = register(Settings.b("Latency", false));
    public Setting<Boolean> durability = register(Settings.b("Item Damage", false));
    public Setting<Boolean> memory = register(Settings.b("Memory Used", false));
    private Setting<SpeedUnit> speedUnit = register(Settings.e("Speed Unit", SpeedUnit.KmH));
    public Setting<ColourCode> firstColour = register(Settings.e("First Colour", ColourCode.WHITE));
    public Setting<ColourCode> secondColour = register(Settings.e("Second Colour", ColourCode.BLUE));
    private Setting<TimeType> timeTypeSetting = register(Settings.e("Time Format", TimeType.HHMMSS));
    public Setting<TimeUnit> timeUnitSetting = register(Settings.e("Time Unit", TimeUnit.h12));

    private enum SpeedUnit {
        MpS, KmH
    }

    private enum TimeType {
        HHMM, HHMMSS, HH
    }

    public enum TimeUnit {
        h24, h12
    }

    public enum ColourCode {
        BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GREY, DARK_GREY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE
    }

    public boolean useUnitKmH() {
        return speedUnit.getValue().equals(SpeedUnit.KmH);
    }

    private String unitType(SpeedUnit s) {
        switch (s) {
            case MpS: return "m/s";
            case KmH: return "km/h";
            default: return "Invalid unit type (mps or kmh)";
        }
    }

    public SimpleDateFormat dateFormatter(TimeUnit timeUnit) {
        SimpleDateFormat formatter;
        switch (timeUnit) {
            case h12:
                formatter = new SimpleDateFormat("HH" + formatTimeString(timeTypeSetting.getValue()), Locale.UK); break;
            case h24:
                formatter = new SimpleDateFormat("hh" + formatTimeString(timeTypeSetting.getValue()), Locale.UK); break;
            default:
                throw new IllegalStateException("Unexpected value: " + timeUnit);
        }
        return formatter;
    }

    private static String formatTimeString(TimeType timeType) {
        switch (timeType) {
            case HHMM: return ":mm";
            case HHMMSS: return ":mm:ss";
            default: return "";
        }
    }

    private String formatTimeColour() {
        String formatted = textColour(secondColour.getValue()) + ":" + textColour(firstColour.getValue());
        return InfoCalculator.time(dateFormatter(timeUnitSetting.getValue())).replace(":", formatted);
    }

    public String formatChatTime() {
        return textColour(secondColour.getValue()) + InfoCalculator.time(dateFormatter(timeUnitSetting.getValue())) + TextFormatting.RESET;
    }

    private String formatTimerSpeed() {
        String formatted = textColour(secondColour.getValue()) + "." + textColour(firstColour.getValue());
        return TimerSpeed.returnGui().replace(".", formatted);
    }

    public String textColour(ColourCode c) {
        switch (c) {
            case BLACK: return TextFormatting.BLACK.toString();
            case DARK_BLUE: return TextFormatting.DARK_BLUE.toString();
            case DARK_GREEN: return TextFormatting.DARK_GREEN.toString();
            case DARK_AQUA: return TextFormatting.DARK_AQUA.toString();
            case DARK_RED: return TextFormatting.DARK_RED.toString();
            case DARK_PURPLE: return TextFormatting.DARK_PURPLE.toString();
            case GOLD: return TextFormatting.GOLD.toString();
            case GREY: return TextFormatting.GRAY.toString();
            case DARK_GREY: return TextFormatting.DARK_GRAY.toString();
            case BLUE: return TextFormatting.BLUE.toString();
            case GREEN: return TextFormatting.GREEN.toString();
            case AQUA: return TextFormatting.AQUA.toString();
            case RED: return TextFormatting.RED.toString();
            case LIGHT_PURPLE: return TextFormatting.LIGHT_PURPLE.toString();
            case YELLOW: return TextFormatting.YELLOW.toString();
            case WHITE: return TextFormatting.WHITE.toString();
            default: return "";
        }
    }

    public ArrayList<String> infoContents() {
        ArrayList<String> infoContents = new ArrayList<>();
        if (version.getValue()) {
            infoContents.add(textColour(firstColour.getValue()) + KamiMod.KAMI_KANJI + textColour(secondColour.getValue()) + " " + KamiMod.MODVER);
        }
        if (username.getValue()) {
            infoContents.add(textColour(firstColour.getValue()) + "Welcome" + textColour(secondColour.getValue()) + " " + mc.player.getName() + "!");
        }
        if (time.getValue()) {
            infoContents.add(textColour(firstColour.getValue()) + formatTimeColour() + TextFormatting.RESET);
        }
        if (tps.getValue()) {
            infoContents.add(textColour(firstColour.getValue()) + InfoCalculator.tps() + textColour(secondColour.getValue()) + " tps");
        }
        if (fps.getValue()) {
            infoContents.add(textColour(firstColour.getValue()) + Minecraft.debugFPS + textColour(secondColour.getValue()) + " fps");
        }
        if (speed.getValue()) {
            infoContents.add(textColour(firstColour.getValue()) + InfoCalculator.speed() + textColour(secondColour.getValue()) + " " + unitType(speedUnit.getValue()));
        }
        if (timerSpeed.getValue()) {
            infoContents.add(textColour(firstColour.getValue()) + formatTimerSpeed() + textColour(secondColour.getValue()) + "t");
        }
        if (ping.getValue()) {
            infoContents.add(textColour(firstColour.getValue()) + InfoCalculator.ping() + textColour(secondColour.getValue()) + " ms");
        }
        if (durability.getValue()) {
            infoContents.add(textColour(firstColour.getValue()) + InfoCalculator.dura() + textColour(secondColour.getValue()) + " dura");
        }
        if (memory.getValue()) {
            infoContents.add(textColour(firstColour.getValue()) + InfoCalculator.memory() + textColour(secondColour.getValue()) + "mB free");
        }
        return infoContents;
    }

    public void onDisable() { this.enable(); }
}
