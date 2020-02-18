package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.misc.CustomChat;

/**
 * @author S-B99
 * Created by S-B99 on 17/02/20
 */
public class CustomChatCommand extends Command {
    public CustomChatCommand() {
        super("customchat", new ChunkBuilder().append("ending").build(), "chat");
        setDescription("Allows you to customize CustomChat's custom setting");
    }

    @Override
    public void call(String[] args) {
        CustomChat cC = (CustomChat) ModuleManager.getModuleByName("CustomChat");
        if (cC == null) {
            Command.sendErrorMessage("&cThe CustomChat module is not available for some reason. Make sure the name you're calling is correct and that you have the module installed!!");
            return;
        }
        if (!cC.isEnabled() || !cC.textMode.getValue().equals(CustomChat.TextMode.CUSTOM)) {
            Command.sendWarningMessage("&6Warning: The CustomChat module is not enabled, or you don't have custom mode enabled!");
            Command.sendWarningMessage("The command will still work, but will not visibly do anything.");
        }
        for (String s : args) {
            if (s == null)
                continue;
            cC.customText.setValue(s);
            Command.sendChatMessage("Set the Custom Text Mode to <" + s + ">");
        }
    }
}
