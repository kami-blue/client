package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.module.modules.chat.AutoReply;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

/**
 * @author S-B99
 * Created by S-B99 on 17/02/20
 */
public class AutoReplyCommand extends Command {
    public AutoReplyCommand() {
        super("autoreply", new ChunkBuilder().append("message").append("=listener").append("-replyCommand").build(), "reply");
        setDescription("Allows you to customize AutoReply's settings");
    }

    @Override
    public void call(String[] args) {
        AutoReply autoReply = (AutoReply) MODULE_MANAGER.getModule(AutoReply.class);
        if (autoReply == null) {
            Command.sendErrorMessage("&cThe AutoReply module is not available for some reason. Make sure the name you're calling is correct and that you have the module installed!!");
            return;
        }
        if (!autoReply.isEnabled()) {
            Command.sendWarningMessage("&6Warning: The AutoReply module is not enabled!");
            Command.sendWarningMessage("The command will still work, but will not visibly do anything.");
        }
        for (String s : args) {
            if (s == null)
                continue;
            if (s.startsWith("=")) {
                String sT = s.replace("=" ,"");
                autoReply.listener.setValue(sT);
                Command.sendChatMessage("Set the AutoReply listener to <" + sT + ">");
                if (!autoReply.customListener.getValue()) {
                    Command.sendWarningMessage("&6Warning: You don't have Custom Listener enabled in AutoReply!");
                    Command.sendWarningMessage("The command will still work, but will not visibly do anything.");
                }
            } else if (s.startsWith("-")) {
                String sT = s.replace("-" ,"");
                autoReply.replyCommand.setValue(sT);
                Command.sendChatMessage("Set the AutoReply reply command to <" + sT + ">");
                if (!autoReply.customReplyCommand.getValue()) {
                    Command.sendWarningMessage("&6Warning: You don't have Custom Reply Command enabled in AutoReply!");
                    Command.sendWarningMessage("The command will still work, but will not visibly do anything.");
                }
            } else {
                autoReply.message.setValue(s);
                Command.sendChatMessage("Set the AutoReply message to <" + s + ">");
                if (!autoReply.customMessage.getValue()) {
                    Command.sendWarningMessage("&6Warning: You don't have Custom Message enabled in AutoReply!");
                    Command.sendWarningMessage("The command will still work, but will not visibly do anything.");
                }
            }
        }
    }
}
