// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.gui;

import net.minecraft.client.Minecraft;
import me.zeroeightsix.kami.util.InfoCalculator;
import net.minecraft.util.text.TextFormatting;
import java.util.ArrayList;
import me.zeroeightsix.kami.module.modules.movement.TimerSpeed;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.TimeUtil;
import me.zeroeightsix.kami.util.ColourUtils;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "InfoOverlay", category = Category.GUI, description = "Configures the game information overlay", showOnArray = ShowOnArray.OFF)
public class InfoOverlay extends Module
{
    private Setting<Boolean> version;
    private Setting<Boolean> username;
    private Setting<Boolean> time;
    private Setting<Boolean> tps;
    private Setting<Boolean> fps;
    private Setting<Boolean> speed;
    private Setting<Boolean> timerSpeed;
    private Setting<Boolean> ping;
    private Setting<Boolean> durability;
    private Setting<Boolean> memory;
    private Setting<SpeedUnit> speedUnit;
    private Setting<ColourUtils.ColourCode> firstColour;
    private Setting<ColourUtils.ColourCode> secondColour;
    private Setting<TimeUtil.TimeType> timeTypeSetting;
    private Setting<TimeUtil.TimeUnit> timeUnitSetting;
    private Setting<Boolean> doLocale;
    
    public InfoOverlay() {
        this.version = this.register(Settings.b("Version", true));
        this.username = this.register(Settings.b("Username", true));
        this.time = this.register(Settings.b("Time", true));
        this.tps = this.register(Settings.b("Ticks Per Second", false));
        this.fps = this.register(Settings.b("Frames Per Second", true));
        this.speed = this.register(Settings.b("Speed", true));
        this.timerSpeed = this.register(Settings.b("Timer Speed", false));
        this.ping = this.register(Settings.b("Latency", false));
        this.durability = this.register(Settings.b("Item Damage", false));
        this.memory = this.register(Settings.b("Memory Used", false));
        this.speedUnit = this.register(Settings.e("Speed Unit", SpeedUnit.KmH));
        this.firstColour = this.register(Settings.e("First Colour", ColourUtils.ColourCode.WHITE));
        this.secondColour = this.register(Settings.e("Second Colour", ColourUtils.ColourCode.BLUE));
        this.timeTypeSetting = this.register(Settings.e("Time Format", TimeUtil.TimeType.HHMMSS));
        this.timeUnitSetting = this.register(Settings.e("Time Unit", TimeUtil.TimeUnit.h12));
        this.doLocale = this.register(Settings.b("Time Show AMPM", true));
    }
    
    public boolean useUnitKmH() {
        return this.speedUnit.getValue().equals(SpeedUnit.KmH);
    }
    
    private String unitType(final SpeedUnit s) {
        switch (s) {
            case MpS: {
                return "m/s";
            }
            case KmH: {
                return "km/h";
            }
            default: {
                return "Invalid unit type (mps or kmh)";
            }
        }
    }
    
    private String formatTimerSpeed() {
        final String formatted = this.textColour(this.secondColour.getValue()) + "." + this.textColour(this.firstColour.getValue());
        return TimerSpeed.returnGui().replace(".", formatted);
    }
    
    public String textColour(final ColourUtils.ColourCode c) {
        return ColourUtils.getStringColour(c);
    }
    
    public ArrayList<String> infoContents() {
        final ArrayList<String> infoContents = new ArrayList<String>();
        if (this.version.getValue()) {
            infoContents.add(this.textColour(this.firstColour.getValue()) + "AstraMod" + this.textColour(this.secondColour.getValue()) + " " + "B5");
        }
        if (this.username.getValue()) {
            infoContents.add(this.textColour(this.firstColour.getValue()) + "Welcome" + this.textColour(this.secondColour.getValue()) + " " + InfoOverlay.mc.field_71439_g.func_70005_c_() + "!");
        }
        if (this.time.getValue()) {
            infoContents.add(this.textColour(this.firstColour.getValue()) + TimeUtil.getFinalTime(this.secondColour.getValue(), this.firstColour.getValue(), this.timeUnitSetting.getValue(), this.timeTypeSetting.getValue(), this.doLocale.getValue()) + TextFormatting.RESET);
        }
        if (this.tps.getValue()) {
            infoContents.add(this.textColour(this.firstColour.getValue()) + InfoCalculator.tps() + this.textColour(this.secondColour.getValue()) + " tps");
        }
        if (this.fps.getValue()) {
            infoContents.add(this.textColour(this.firstColour.getValue()) + Minecraft.field_71470_ab + this.textColour(this.secondColour.getValue()) + " fps");
        }
        if (this.speed.getValue()) {
            infoContents.add(this.textColour(this.firstColour.getValue()) + InfoCalculator.speed() + this.textColour(this.secondColour.getValue()) + " " + this.unitType(this.speedUnit.getValue()));
        }
        if (this.timerSpeed.getValue()) {
            infoContents.add(this.textColour(this.firstColour.getValue()) + this.formatTimerSpeed() + this.textColour(this.secondColour.getValue()) + "t");
        }
        if (this.ping.getValue()) {
            infoContents.add(this.textColour(this.firstColour.getValue()) + InfoCalculator.ping() + this.textColour(this.secondColour.getValue()) + " ms");
        }
        if (this.durability.getValue()) {
            infoContents.add(this.textColour(this.firstColour.getValue()) + InfoCalculator.dura() + this.textColour(this.secondColour.getValue()) + " dura");
        }
        if (this.memory.getValue()) {
            infoContents.add(this.textColour(this.firstColour.getValue()) + InfoCalculator.memory() + this.textColour(this.secondColour.getValue()) + "mB free");
        }
        return infoContents;
    }
    
    public void onDisable() {
        this.enable();
    }
    
    private enum SpeedUnit
    {
        MpS, 
        KmH;
    }
}
