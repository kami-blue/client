package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.command.syntax.parsers.ModuleParser;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.client.CommandConfig;
import me.zeroeightsix.kami.util.Macro;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;

/**
 * Created by 086 on 17/11/2017.
 */
public class ToggleCommand extends Command {

    public ToggleCommand() {
        super("toggle", new ChunkBuilder()
                .append("module", true, new ModuleParser())
                .build(), "t");
        setDescription("Quickly toggle a module on and off");
    }

    @Override
    public void call(String[] args) {
        if (args.length == 0) {
            sendChatMessage("Please specify a module!");
            return;
        }
        try {
            Class<?> aClass = Class.forName(args[0]);
            Module m = MODULE_MANAGER.getModule((Class<? extends Module>) aClass);
            m.toggle();
            if (!MODULE_MANAGER.getModuleT(CommandConfig.class).toggleMessages.getValue()) {
                sendChatMessage(m.name.getValue() + (m.isEnabled() ? " &aenabled" : " &cdisabled"));
            }
        } catch (ModuleManager.ModuleNotFoundException x) {
            sendChatMessage("Unknown module '" + args[0] + "'");
        } catch (Throwable t) {
            sendChatMessage("Bruh");
        }
    }
}
