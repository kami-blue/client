package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.module.Module;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

/**
 * @author S-B99
 * Created by S-B99 on 17/02/20
 */
public class DescriptionCommand extends Command {
    public DescriptionCommand() {
        super("description", new ChunkBuilder().append("module").build(), "tooltip");
        setDescription("Prints a module's description into the chat");
    }

    @Override
    public void call(String[] args) {
        for (String s : args) {
            if (s == null)
                continue;
            Module module = MODULE_MANAGER.getModule(s);
            Command.sendChatMessage(module.getChatName() + "Description: &7" + module.getDescription());
        }
    }
}
