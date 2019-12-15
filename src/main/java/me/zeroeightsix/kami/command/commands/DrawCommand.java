package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.command.syntax.parsers.ModuleParser;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;

/**
 * Created by milse113 on 15/12/2019.
 */
public class DrawCommand extends Command {

    public DrawCommand() {
        super("draw", new ChunkBuilder()
                .append("module", true, new ModuleParser())
                .build());
        setDescription("Show or hide a module");
    }

    @Override
    public void call(String[] args) {
        if (args.length == 0) {
            Command.sendChatMessage("Please specify a module!");
            return;
        }
        Module m = ModuleManager.getModuleByName(args[0]);
        if (m == null) {
            Command.sendChatMessage("Unknown module '" + args[0] + "'");
            return;
        }
        m.toggleDrawn();
        Command.sendChatMessage(m.getName() + (m.isEnabled() ? " &ahas been revealed" : " &chas been hidden"));
    }
}
