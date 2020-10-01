package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;

import static me.zeroeightsix.kami.util.text.MessageSendHelper.sendServerMessage;

/**
 * @author l1ving
 * Created by l1ving on 12/03/20
 */
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
        sendServerMessage(message.toString());
    }
}