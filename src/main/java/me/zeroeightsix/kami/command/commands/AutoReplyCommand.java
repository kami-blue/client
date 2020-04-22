package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.module.modules.chat.AutoReply;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static me.zeroeightsix.kami.util.MessageSendHelper.*;

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 17/02/20
 */
public class AutoReplyCommand extends Command {
    public AutoReplyCommand() {
        super("autoreply", new ChunkBuilder().append("message").append("=listener").append("-replyCommand").build(), "reply");
        setDescription("Allows you to customize AutoReply's settings");
    }

    @Override
    public void call(String[] args) {
        AutoReply autoReply = MODULE_MANAGER.getModuleT(AutoReply.class);
        if (autoReply == null) {
            sendErrorMessage("&cThe AutoReply module is not available for some reason. Make sure the name you're calling is correct and that you have the module installed!!");
            return;
        }
        if (!autoReply.isEnabled()) {
            sendWarningMessage("&6Warning: The AutoReply module is not enabled!");
            sendWarningMessage("The command will still work, but will not visibly do anything.");
        }
        for (String s : args) {
            if (s == null)
                continue;
            if (s.startsWith("=")) {
                String sT = s.replace("=" ,"");
                autoReply.listener.setValue(sT);
                sendChatMessage("Set the AutoReply listener to <" + sT + ">");
                if (!autoReply.customListener.getValue()) {
                    sendWarningMessage("&6Warning: You don't have Custom Listener enabled in AutoReply!");
                    sendWarningMessage("The command will still work, but will not visibly do anything.");
                }
            } else if (s.startsWith("-")) {
                String sT = s.replace("-" ,"");
                autoReply.replyCommand.setValue(sT);
                sendChatMessage("Set the AutoReply reply command to <" + sT + ">");
                if (!autoReply.customReplyCommand.getValue()) {
                    sendWarningMessage("&6Warning: You don't have Custom Reply Command enabled in AutoReply!");
                    sendWarningMessage("The command will still work, but will not visibly do anything.");
                }
            } else {
                autoReply.message.setValue(s);
                sendChatMessage("Set the AutoReply message to <" + s + ">");
                if (!autoReply.customMessage.getValue()) {
                    sendWarningMessage("&6Warning: You don't have Custom Message enabled in AutoReply!");
                    sendWarningMessage("The command will still work, but will not visibly do anything.");
                }
            }
        }
    }
}
