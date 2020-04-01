package me.zeroeightsix.kami.util;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.module.modules.gui.CommandConfig;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

/**
 * @author S-B99
 */
public class CommandUtil {
    public static void runAliases(Command command) {
        if (!((CommandConfig) MODULE_MANAGER.getModule(CommandConfig.class)).aliasInfo.getValue()) return;
        int amount = command.getAliases().size();
        if (amount > 0) {
            Command.sendChatMessage("'" + command.getLabel() + "' has " + grammar1(amount) + "alias" + grammar2(amount));
            Command.sendChatMessage(command.getAliases().toString());
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
