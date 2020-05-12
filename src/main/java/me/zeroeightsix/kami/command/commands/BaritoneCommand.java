package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;

import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;

/**
 * Created by Dewy on the 17th of April, 2020
 */
public class BaritoneCommand extends Command {

    public BaritoneCommand() {
        super("baritone", null);
        setDescription("Configure baritone using the Baritone module in the client category or by using its command system. Try '#help'.");
    }

    @Override
    public void call(String[] args) {
        sendChatMessage("KAMI Blue has Baritone integration. To configure Baritone, use the Baritone module or Baritone's own command system. Try #help for a command list.");
    }
}
