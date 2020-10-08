package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.module.modules.combat.VisualRange;

import static me.zeroeightsix.kami.util.text.MessageSendHelper.*;

public class VisualRangeJoinCommand extends Command {
    public VisualRangeJoinCommand() {
        super("customjoin", new ChunkBuilder().append("message").build(), "message");
        setDescription("Allows you to customize VisualRange's join message");
    }

    @Override
    public void call(String[] args) {
        if (VisualRange.INSTANCE == null) {
            sendErrorMessage("&cThe VisualRange Join Option Not Activated. Make sure to enable it for the message to appear.");
            return;
        }

        if (args[0] == null) return;

        VisualRange.INSTANCE.getVisualJoinMessage().setValue(args[0]);
        sendChatMessage("Set the VisualRange join message to '&7" + args[0] + "&f'");

        if (!VisualRange.INSTANCE.getCustomjoin().getValue()) {
            sendWarningMessage("&6Warning:&f You don't have '&7Custom join Message&f' enabled in VisualRange!");
            sendWarningMessage("The command will still work, but will not visibly do anything.");
        }
    }
}