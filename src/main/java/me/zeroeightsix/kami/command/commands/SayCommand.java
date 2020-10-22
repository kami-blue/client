package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.util.text.MessageSendHelper;

public class SayCommand extends Command {
    public SayCommand() {
        super("say", new ChunkBuilder().append("message").build());
        setDescription("Allows you to send any message, even with a prefix in it");
    }

    @Override
    public void call(String[] args) {
        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            if (arg != null) {
                message.append(" ").append(arg);
            }
        }
        MessageSendHelper.sendServerMessage(message.toString(), this, Integer.MAX_VALUE);
    }
}