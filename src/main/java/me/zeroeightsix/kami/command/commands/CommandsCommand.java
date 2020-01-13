package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.SyntaxChunk;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;

/**
 * Created by 086 on 12/11/2017.
 */
public class CommandsCommand extends Command {

    public CommandsCommand() {
        super("commands", SyntaxChunk.EMPTY, "cmds", "cmd");
        setDescription("Gives you this list of commands");
    }

    @Override
    public void call(String[] args) {
        KamiMod.getInstance().getCommandManager().getCommands().stream().sorted(Comparator.comparing(command -> command.getLabel())).forEach(command ->
            Command.sendChatMessage("&7" + Command.getCommandPrefix() + command.getLabel() + "&r ~ &8" + command.getDescription() + "\n&5Aliases: " + StringUtils.join(command.getAliases(), ", &5"))
        );
    }
}
