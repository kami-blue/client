package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.module.modules.combat.AutoEZ;

import static me.zeroeightsix.kami.util.text.MessageSendHelper.*;

public class AutoEZCommand extends Command {
    public AutoEZCommand() {
        super("autoez", new ChunkBuilder().append("message").build());
        setDescription("Allows you to customize AutoEZ's custom setting");
    }

    @Override
    public void call(String[] args) {
        if (AutoEZ.INSTANCE == null) {
            sendErrorMessage("&cThe AutoEZ module is not available for some reason. Make sure the name you're calling is correct and that you have the module installed!!");
            return;
        }
        if (!AutoEZ.INSTANCE.isEnabled()) {
            sendWarningMessage("&6Warning: The AutoEZ module is not enabled!");
            sendWarningMessage("The command will still work, but will not visibly do anything.");
        }
        if (!AutoEZ.INSTANCE.getMessageMode().getValue().equals(AutoEZ.MessageMode.CUSTOM)) {
            sendWarningMessage("&6Warning: You don't have custom mode enabled in AutoEZ!");
            sendWarningMessage("The command will still work, but will not visibly do anything.");
        }
        for (String s : args) {
            if (s == null)
                continue;
            AutoEZ.INSTANCE.getCustomText().setValue(s);
            sendChatMessage("Set the Custom Mode to <" + s + ">");
        }
    }
}
