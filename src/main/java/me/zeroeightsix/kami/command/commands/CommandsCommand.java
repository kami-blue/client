package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.SyntaxChunk;

import java.util.Comparator;

import static me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage;

/**
 * Created by 086 on 12/11/2017.
 */
public class CommandsCommand extends Command {

    public CommandsCommand() {
        super("commands", SyntaxChunk.EMPTY, "cmds");
        setDescription("Gives you this list of commands");
    }

    @Override
    public void call(String[] args) {
        KamiMod.getInstance().getCommandManager().getCommands().stream().sorted(Comparator.comparing(Command::getLabel)).forEach(command ->
                sendChatMessage("&f" + Command.getCommandPrefix() + command.getLabel() + "&r ~ &7" + command.getDescription())
        );
    }
}
