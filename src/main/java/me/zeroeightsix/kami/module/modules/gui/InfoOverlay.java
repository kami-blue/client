package me.zeroeightsix.kami.module.modules.gui;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.modules.movement.TimerSpeed;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.ColourUtils;
import me.zeroeightsix.kami.util.InfoCalculator;
import me.zeroeightsix.kami.util.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;

import static me.zeroeightsix.kami.util.ColourUtils.getStringColour;

/**
 * @author S-B99
 * Created by S-B99 on 04/12/19
 * Updated by S-B99 on 06/02/20
 */
@Module.Info(name = "InfoOverlay", category = Module.Category.GUI, description = "Configures game information overlay", showOnArray = Module.ShowOnArray.OFF)
public class InfoOverlay extends Module {
    private Setting<Boolean> version = register(Settings.b("Version", true));
    private Setting<Boolean> username = register(Settings.b("Username", true));
    private Setting<Boolean> time = register(Settings.b("Time", true));
    private Setting<Boolean> tps = register(Settings.b("Ticks Per Second", false));
    private Setting<Boolean> fps = register(Settings.b("Frames Per Second", true));
    private Setting<Boolean> speed = register(Settings.b("Speed", true));
    private Setting<Boolean> timerSpeed = register(Settings.b("Timer Speed", false));
    private Setting<Boolean> ping = register(Settings.b("Latency", false));
    private Setting<Boolean> durability = register(Settings.b("Item Damage", false));
    private Setting<Boolean> memory = register(Settings.b("Memory Used", false));
    private Setting<SpeedUnit> speedUnit = register(Settings.e("Speed Unit", SpeedUnit.KmH));
    private Setting<ColourUtils.ColourCode> firstColour = register(Settings.e("First Colour", ColourUtils.ColourCode.WHITE));
    private Setting<ColourUtils.ColourCode> secondColour = register(Settings.e("Second Colour", ColourUtils.ColourCode.BLUE));
    private Setting<TimeUtil.TimeType> timeTypeSetting = register(Settings.e("Time Format", TimeUtil.TimeType.HHMMSS));
    private Setting<TimeUtil.TimeUnit> timeUnitSetting = register(Settings.e("Time Unit", TimeUtil.TimeUnit.h12));

    private enum SpeedUnit {
        MpS, KmH
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



    private String formatTimerSpeed() {
        String formatted = textColour(secondColour.getValue()) + "." + textColour(firstColour.getValue());
        return TimerSpeed.returnGui().replace(".", formatted);
    }

    public String textColour(ColourUtils.ColourCode c) {
        return getStringColour(c);
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
            infoContents.add(textColour(firstColour.getValue()) + TimeUtil.getFinalTime(secondColour.getValue(), firstColour.getValue(), timeUnitSetting.getValue(), timeTypeSetting.getValue()) + TextFormatting.RESET);
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
