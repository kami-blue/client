// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami;

import me.zeroeightsix.kami.module.ModuleManager;
import club.minnced.discord.rpc.DiscordEventHandlers;
import me.zeroeightsix.kami.module.modules.gui.DiscordSettings;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;

public class DiscordPresence
{
    public static DiscordRichPresence presence;
    private static boolean hasStarted;
    private static final DiscordRPC rpc;
    private static String details;
    private static String state;
    private static DiscordSettings discordSettings;
    
    public static void start() {
        KamiMod.log.info("Starting Discord RPC");
        if (DiscordPresence.hasStarted) {
            return;
        }
        DiscordPresence.hasStarted = true;
        final DiscordEventHandlers handlers = new DiscordEventHandlers();
        handlers.disconnected = ((var1, var2) -> KamiMod.log.info("Discord RPC disconnected, var1: " + var1 + ", var2: " + var2));
        DiscordPresence.rpc.Discord_Initialize("717788237799620630", handlers, true, "");
        DiscordPresence.presence.startTimestamp = System.currentTimeMillis() / 1000L;
        setRpcFromSettings();
        new Thread(DiscordPresence::setRpcFromSettingsNonInt, "Discord-RPC-Callback-Handler").start();
        KamiMod.log.info("Discord RPC initialised successfully");
    }
    
    private static void setRpcFromSettingsNonInt() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                DiscordPresence.rpc.Discord_RunCallbacks();
                DiscordPresence.discordSettings = (DiscordSettings)ModuleManager.getModuleByName("DiscordRPC");
                final String separator = " | ";
                DiscordPresence.details = DiscordPresence.discordSettings.getLine(DiscordPresence.discordSettings.line1Setting.getValue()) + separator + DiscordPresence.discordSettings.getLine(DiscordPresence.discordSettings.line3Setting.getValue());
                DiscordPresence.state = DiscordPresence.discordSettings.getLine(DiscordPresence.discordSettings.line2Setting.getValue()) + separator + DiscordPresence.discordSettings.getLine(DiscordPresence.discordSettings.line4Setting.getValue());
                DiscordPresence.presence.details = DiscordPresence.details;
                DiscordPresence.presence.state = DiscordPresence.state;
                DiscordPresence.rpc.Discord_UpdatePresence(DiscordPresence.presence);
            }
            catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                Thread.sleep(4000L);
            }
            catch (InterruptedException e3) {
                e3.printStackTrace();
            }
        }
    }
    
    private static void setRpcFromSettings() {
        DiscordPresence.discordSettings = (DiscordSettings)ModuleManager.getModuleByName("DiscordRPC");
        DiscordPresence.details = DiscordPresence.discordSettings.getLine(DiscordPresence.discordSettings.line1Setting.getValue()) + " " + DiscordPresence.discordSettings.getLine(DiscordPresence.discordSettings.line3Setting.getValue());
        DiscordPresence.state = DiscordPresence.discordSettings.getLine(DiscordPresence.discordSettings.line2Setting.getValue()) + " " + DiscordPresence.discordSettings.getLine(DiscordPresence.discordSettings.line4Setting.getValue());
        DiscordPresence.presence.details = DiscordPresence.details;
        DiscordPresence.presence.state = "astraMod_client";
        DiscordPresence.presence.largeImageKey = "large";
        DiscordPresence.presence.largeImageText = "AstraMod";
        DiscordPresence.presence.smallImageKey = "astraMod";
        DiscordPresence.presence.smallImageText = "B5";
        DiscordPresence.rpc.Discord_UpdatePresence(DiscordPresence.presence);
    }
    
    static {
        rpc = DiscordRPC.INSTANCE;
        DiscordPresence.presence = new DiscordRichPresence();
        DiscordPresence.hasStarted = false;
    }
}
