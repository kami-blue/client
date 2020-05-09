package me.zeroeightsix.kami.util;

import java.util.regex.Pattern;

/**
 * A helper to detect certain messages and return a boolean or message
 *
 * @author dominikaaaa
 * @see me.zeroeightsix.kami.module.modules.chat.DiscordNotifs
 */
public class MessageDetectionHelper {
    public static String getMessageType(boolean direct, boolean directSent, String message, String server) {
        if (isDirect(direct, message)) return "You got a direct message!\n";
        if (isDirectOther(directSent, message)) return "You sent a direct message!\n";
        if (message.equals("KamiBlueMessageType1")) return "Connected to " + server;
        if (message.equals("KamiBlueMessageType2")) return "Disconnected from " + server;
        return "";
    }

    public static boolean isDirect(boolean direct, String message) {
        return direct && Pattern.compile("^([0-9A-z_])+ whispers:.*").matcher(message).find();
    }

    public static boolean isDirectOther(boolean directSent, String message) {
        return directSent && Pattern.compile("^to ([0-9A-z_])+:.*").matcher(message).find();
    }

    public static boolean isTPA(boolean tpa, String message) {
        return tpa && Pattern.compile("^([0-9A-z_])+ has requested to teleport to you\\..*").matcher(message).find();
    }

    public static boolean isQueue(boolean queue, String message) {
        if (queue && message.contains("Position in queue:")) return true;
        else return queue && message.contains("2b2t is full");
    }

    public static boolean isImportantQueue(boolean importantPings, String message) {
        return importantPings && (
                message.equals("Position in queue: 1") ||
                        message.equals("Position in queue: 2") ||
                        message.equals("Position in queue: 3"));
    }

    public static boolean isRestart(boolean restart, String message) {
        return restart && message.contains("[SERVER] Server restarting in");
    }

    public static boolean shouldSend(boolean all, boolean restart, boolean direct, boolean directSent, boolean queue, boolean importantPings, String message) {
        if (all) return true;
        else
            return isRestart(restart, message) || isDirect(direct, message) || isDirectOther(directSent, message) || isQueue(queue, message) || isImportantQueue(importantPings, message);
    }

}
