// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.command.syntax.SyntaxParser;
import me.zeroeightsix.kami.command.syntax.parsers.ModuleParser;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.command.Command;

public class RenameModuleCommand extends Command
{
    public RenameModuleCommand() {
        super("renamemodule", new ChunkBuilder().append("module", true, new ModuleParser()).append("name").build());
        this.setDescription("Rename a module to something else");
    }
    
    @Override
    public void call(final String[] args) {
        if (args.length == 0) {
            Command.sendChatMessage("Please specify a module!");
            return;
        }
        final Module module = ModuleManager.getModuleByName(args[0]);
        if (module == null) {
            Command.sendChatMessage("Unknown module '" + args[0] + "'!");
            return;
        }
        final String name = (args.length == 1) ? module.getOriginalName() : args[1];
        if (!name.matches("[a-zA-Z]+")) {
            Command.sendChatMessage("Name must be alphabetic!");
            return;
        }
        Command.sendChatMessage("&4" + module.getName() + "&r renamed to &4" + name);
        module.setName(name);
    }
}
