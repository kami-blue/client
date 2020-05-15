package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.command.syntax.parsers.BaritoneParser;
import me.zeroeightsix.kami.util.MessageSendHelper;

/**
 * @author dominikaaaa
 */
public class BaritoneCommand extends Command {

    public BaritoneCommand() { // TODO: add autocompletion
        super("baritone", new ChunkBuilder().append("arg", true, new BaritoneParser()).build(), "b");
        setDescription("Runs baritone commands!");
    }

    @Override
    public void call(String[] args) { // the last arg is null in kami commands, so instead make a new String[] that's 1 smaller
        String[] newArgs = new String[ args.length - 1];
        if (args.length - 1 >= 0) System.arraycopy(args, 0, newArgs, 0, args.length - 1);
        MessageSendHelper.sendBaritoneCommand(newArgs);
    }
}
