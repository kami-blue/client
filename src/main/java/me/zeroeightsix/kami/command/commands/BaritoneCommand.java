package me.zeroeightsix.kami.command.commands;

import baritone.Baritone;
import baritone.api.BaritoneAPI;
import me.zeroeightsix.kami.command.Command;

import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;

/**
 * Created by Dewy on the 17th of April, 2020
 */
public class BaritoneCommand extends Command {

    public BaritoneCommand() {
        super("baritone", null, "b");
        setDescription("Runs baritone commands!");
    }

    @Override
    public void call(String[] args) {
        StringBuilder command = new StringBuilder();
        for (String arg : args) {
            command.append(" ").append(arg);
        }
        // TODO: leij dms 
//        BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute()
    }
}
