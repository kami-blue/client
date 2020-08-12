// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.gui;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.DiscordPresence;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "DiscordRPC", category = Category.GUI, description = "Discord Rich Presence")
public class DiscordSettings extends Module
{
    public Setting<Boolean> startupGlobal;
    public Setting<Boolean> coordsConfirm;
    public Setting<LineInfo> line1Setting;
    public Setting<LineInfo> line3Setting;
    public Setting<LineInfo> line2Setting;
    public Setting<LineInfo> line4Setting;
    private static long startTime;
    
    public DiscordSettings() {
        this.startupGlobal = this.register(Settings.b("Enable Automatically", true));
        this.coordsConfirm = this.register(Settings.b("Coords Confirm", false));
        this.line1Setting = this.register(Settings.e("Line 1 Left", LineInfo.VERSION));
        this.line3Setting = this.register(Settings.e("Line 1 Right", LineInfo.USERNAME));
        this.line2Setting = this.register(Settings.e("Line 2 Left", LineInfo.SERVERIP));
        this.line4Setting = this.register(Settings.e("Line 2 Right", LineInfo.HEALTH));
    }
    
    public String getLine(final LineInfo line) {
        switch (line) {
            case VERSION: {
                return "2.0";
            }
            case WORLD: {
                if (DiscordSettings.mc.func_71387_A()) {
                    return "Ezzz";
                }
                if (DiscordSettings.mc.func_147104_D() != null) {
                    return "Too bad!";
                }
                return "Ez mad!";
            }
            case USERNAME: {
                if (DiscordSettings.mc.field_71439_g != null) {
                    return DiscordSettings.mc.field_71439_g.func_70005_c_();
                }
                return "(Not logged in)";
            }
            case HEALTH: {
                if (DiscordSettings.mc.field_71439_g != null) {
                    return "(" + (int)DiscordSettings.mc.field_71439_g.func_110143_aJ() + " hearts)";
                }
                return "(No hp ;( )";
            }
            case SERVERIP: {
                if (DiscordSettings.mc.func_147104_D() != null) {
                    return DiscordSettings.mc.func_147104_D().field_78845_b;
                }
                return "(Not Online!)";
            }
            case COORDS: {
                if (DiscordSettings.mc.field_71439_g != null && this.coordsConfirm.getValue()) {
                    return "(" + (int)DiscordSettings.mc.field_71439_g.field_70165_t + " " + (int)DiscordSettings.mc.field_71439_g.field_70163_u + " " + (int)DiscordSettings.mc.field_71439_g.field_70161_v + ")";
                }
                return "(No coords)";
            }
            default: {
                return "";
            }
        }
    }
    
    public void onEnable() {
        DiscordPresence.start();
    }
    
    @Override
    public void onUpdate() {
        if (DiscordSettings.startTime == 0L) {
            DiscordSettings.startTime = System.currentTimeMillis();
        }
        if (DiscordSettings.startTime + 10000L <= System.currentTimeMillis()) {
            if ((this.line1Setting.getValue().equals(LineInfo.COORDS) || this.line2Setting.getValue().equals(LineInfo.COORDS) || this.line3Setting.getValue().equals(LineInfo.COORDS) || this.line4Setting.getValue().equals(LineInfo.COORDS)) && !this.coordsConfirm.getValue() && DiscordSettings.mc.field_71439_g != null) {
                Command.sendWarningMessage("[DiscordRPC] Warning: In order to use the coords option please enable the coords confirmation option. This will display your coords on the discord rpc. Do NOT use this if you do not want your coords displayed");
            }
            DiscordSettings.startTime = System.currentTimeMillis();
        }
    }
    
    static {
        DiscordSettings.startTime = 0L;
    }
    
    public enum LineInfo
    {
        VERSION, 
        WORLD, 
        USERNAME, 
        HEALTH, 
        SERVERIP, 
        COORDS, 
        NONE;
    }
}
