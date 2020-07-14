package me.zeroeightsix.kami;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRichPresence;
import me.zeroeightsix.kami.module.modules.misc.DiscordRPC;
import me.zeroeightsix.kami.util.RichPresence;
import net.minecraft.client.Minecraft;

import static me.zeroeightsix.kami.KamiMod.APP_ID;
import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

/**
 * @author dominikaaaa
 * Updated by dominikaaaa on 13/01/20
 * Updated (slightly) by Dewy on 3rd April 2020
 * Updated (slightly) by sourTaste000 on 13/7/2020 (legit 3 lines)
 */
public class DiscordPresence {
    public static DiscordRichPresence presence;
    private static boolean connected;
    private static final club.minnced.discord.rpc.DiscordRPC rpc;
    private static String details;
    private static String state;
    private static DiscordRPC discordRPC;

    public static void start() {
        KamiMod.log.info("Starting Discord RPC");
        if (DiscordPresence.connected) return;
        DiscordPresence.connected = true;

        final DiscordEventHandlers handlers = new DiscordEventHandlers();
        DiscordPresence.rpc.Discord_Initialize(APP_ID, handlers, true, "");
        DiscordPresence.presence.startTimestamp = System.currentTimeMillis() / 1000L;

        /* update rpc normally */
        setRpcFromSettings(true);

        /* update rpc while thread isn't interrupted  */
        new Thread(DiscordPresence::setRpcWithDelay, "Discord-RPC-Callback-Handler").start();
        KamiMod.log.info("Discord RPC initialised successfully");
    }

    public static void end() {
        KamiMod.log.info("Shutting down Discord RPC...");

        DiscordPresence.connected = false;

        DiscordPresence.rpc.Discord_Shutdown();
    }

    private static void setRpcWithDelay() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                setRpcFromSettings(false);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                Thread.sleep(4000L);
            } catch (InterruptedException e3) {
                e3.printStackTrace();
            }
        }
    }

    private static void setRpcFromSettings(boolean image) {
        discordRPC = MODULE_MANAGER.getModuleT(DiscordRPC.class);
        details = discordRPC.getLine(discordRPC.line1Setting.getValue()) + getSeparator(0) + discordRPC.getLine(discordRPC.line3Setting.getValue());
        state = discordRPC.getLine(discordRPC.line2Setting.getValue()) + getSeparator(1) + discordRPC.getLine(discordRPC.line4Setting.getValue());
        DiscordPresence.presence.details = details;
        DiscordPresence.presence.state = state;
        if (image) {
            DiscordPresence.presence.largeImageKey = "kami";
            DiscordPresence.presence.largeImageText = "kamiblue.org";
        }
        DiscordPresence.rpc.Discord_UpdatePresence(DiscordPresence.presence);
    }

    private static String getSeparator(int line) {
        if (line == 0) {
            if (discordRPC.line1Setting.getValue() == DiscordRPC.LineInfo.NONE || discordRPC.line3Setting.getValue() == DiscordRPC.LineInfo.NONE) {
                return " ";
            } else {
                return " | ";
            }
        } else {
            if (discordRPC.line2Setting.getValue() == DiscordRPC.LineInfo.NONE || discordRPC.line4Setting.getValue() == DiscordRPC.LineInfo.NONE) {
                return " ";
            } else {
                return " | ";
            }
        }
    }

    static {
        rpc = club.minnced.discord.rpc.DiscordRPC.INSTANCE;
        DiscordPresence.presence = new DiscordRichPresence();
        DiscordPresence.connected = false;
    }

    public static void setCustomIcons() {
        if (RichPresence.INSTANCE.customUsers != null) {
            for (RichPresence.CustomUser user : RichPresence.INSTANCE.customUsers) {
                if (user.uuid.equalsIgnoreCase(Minecraft.getMinecraft().session.getProfile().getId().toString())) {
                    switch (Integer.parseInt(user.type)) {
                        case 0: {
                            presence.smallImageKey = "booster";
                            presence.smallImageText = "booster uwu";
                            break;
                        }
                        case 1: {
                            presence.smallImageKey = "inviter";
                            presence.smallImageText = "inviter owo";
                            break;
                        }
                        case 2: {
                            presence.smallImageKey = "giveaway";
                            presence.smallImageText = "giveaway winner";
                            break;
                        }
                        case 3: {
                            presence.smallImageKey = "contest";
                            presence.smallImageText = "contest winner";
                            break;
                        }
                        case 4: {
                            presence.smallImageKey = "nine";
                            presence.smallImageText = "900th member";
                            break;
                        }
                        case 5: {
                            presence.smallImageKey = "github1";
                            presence.smallImageText = "contributor!! uwu";
                            break;
                        }
                        default: {
                            presence.smallImageKey = "donator2";
                            presence.smallImageText = "donator <3";
                            break;
                        }
                    }
                }
            }
        }
    }
}
