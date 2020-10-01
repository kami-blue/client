package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.module.modules.chat.CustomChat;

import static me.zeroeightsix.kami.util.text.MessageSendHelper.*;

/**
 * @author l1ving
 * Created by l1ving on 17/02/20
 */
public class CustomChatCommand extends Command {
    public CustomChatCommand() {
        super("customchat", new ChunkBuilder().append("ending").build(), "chat");
        setDescription("Allows you to customize CustomChat's custom setting");
    }

    @Override
    public void call(String[] args) {
        if (CustomChat.INSTANCE == null) {
            sendErrorMessage("&cThe CustomChat module is not available for some reason. Make sure the name you're calling is correct and that you have the module installed!!");
            return;
        }
        if (!CustomChat.INSTANCE.isEnabled()) {
            sendWarningMessage("&6Warning: The CustomChat module is not enabled!");
            sendWarningMessage("The command will still work, but will not visibly do anything.");
        }
        if (!CustomChat.INSTANCE.getTextMode().getValue().equals(CustomChat.TextMode.CUSTOM)) {
            sendWarningMessage("&6Warning: You don't have custom mode enabled in CustomChat!");
            sendWarningMessage("The command will still work, but will not visibly do anything.");
        }
        for (String s : args) {
            if (s == null)
                continue;
            CustomChat.INSTANCE.getCustomText().setValue(s);
            sendChatMessage("Set the Custom Text Mode to <" + s + ">");
        }
    }
}
