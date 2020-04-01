package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.command.syntax.parsers.ModuleParser;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

public class RenameModuleCommand extends Command {

    public RenameModuleCommand() {
        super("renamemodule", new ChunkBuilder().append("module", true, new ModuleParser()).append("name").build());
        setDescription("Rename a module to something else");
    }

    @Override
    public void call(String[] args) {
        if (args.length == 0) {
            sendChatMessage("Please specify a module!");
            return;
        }

        try {
            Module module = MODULE_MANAGER.getModule(args[0]);
            String name = args.length == 1 ? module.getOriginalName() : args[1];

            if (!(name.matches("[a-zA-Z]+"))) {
                sendChatMessage("Name must be alphabetic!");
                return;
            }

            sendChatMessage("&b" + module.getName() + "&r renamed to &b" + name);
            module.setName(name);
        } catch (ModuleManager.ModuleNotFoundException x) {
            sendChatMessage("Unknown module '" + args[0] + "'!");
            return;
        }
    }

}
