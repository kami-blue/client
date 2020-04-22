package me.zeroeightsix.kami.util;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.module.modules.client.CommandConfig;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;

/**
 * @author dominikaaaa
 */
public class CommandUtil {
    public static void runAliases(Command command) {
        if (!MODULE_MANAGER.getModuleT(CommandConfig.class).aliasInfo.getValue()) return;
        int amount = command.getAliases().size();
        if (amount > 0) {
            sendChatMessage("'" + command.getLabel() + "' has " + grammar1(amount) + "alias" + grammar2(amount));
            sendChatMessage(command.getAliases().toString());
        }
    }

    private static String grammar1(int amount) {
        if (amount == 1) return "an ";
        return amount + " ";
    }

    private static String grammar2(int amount) {
        if (amount == 1) return "!";
        return "es!";
    }
}
