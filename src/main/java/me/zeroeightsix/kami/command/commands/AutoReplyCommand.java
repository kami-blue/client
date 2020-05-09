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
        super("autoreply", new ChunkBuilder().append("message").build(), "reply");
        setDescription("Allows you to customize AutoReply's settings");
    }

    @Override
    public void call(String[] args) {
        AutoReply autoReply = MODULE_MANAGER.getModuleT(AutoReply.class);
        if (autoReply == null) {
            sendErrorMessage("&cThe AutoReply module is not available for some reason. Make sure the name you're calling is correct and that you have the module installed!!");
            return;
        }

        if (args[0] == null) return;

        autoReply.message.setValue(args[0]);
        sendChatMessage("Set the AutoReply message to '&7" + args[0] + "&f'");

        if (!autoReply.customMessage.getValue()) {
            sendWarningMessage("&6Warning:&f You don't have '&7Custom Message&f' enabled in AutoReply!");
            sendWarningMessage("The command will still work, but will not visibly do anything.");
        }
    }
}
