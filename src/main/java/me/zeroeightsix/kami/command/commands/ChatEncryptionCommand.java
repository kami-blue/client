package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.module.modules.chat.ChatEncryption;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static me.zeroeightsix.kami.util.MessageSendHelper.*;

public class ChatEncryptionCommand extends Command {
    public ChatEncryptionCommand() {
        super("chatencryption", new ChunkBuilder().append("delimiter").build(), "delimiter");
        setDescription("Allows you to customize ChatEncryption's delimiter");
    }

    @Override
    public void call(String[] args) {
        ChatEncryption ce = MODULE_MANAGER.getModuleT(ChatEncryption.class);
        if (ce == null) {
            sendErrorMessage("&cThe ChatEncryption module is not available for some reason. Make sure the name you're calling is correct and that you have the module installed!!");
            return;
        }
        if (!ce.isEnabled()) {
            sendWarningMessage("&6Warning: The ChatEncryption module is not enabled!");
            sendWarningMessage("The command will still work, but will not visibly do anything.");
        }
        for (String s : args) {
            if (s == null)
                continue;
            if (s.length() > 1) {
                sendErrorMessage("Delimiter can only be 1 character long");
                return;
            }
            ChatEncryption.delimiterValue.setValue(s);
            sendChatMessage("Set the delimiter to <" + s + ">");
        }
    }
}
