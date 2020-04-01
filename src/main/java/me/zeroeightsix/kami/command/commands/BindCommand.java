package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.command.syntax.parsers.ModuleParser;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.builder.SettingBuilder;
import me.zeroeightsix.kami.util.Wrapper;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

/**
 * Created by 086 on 12/11/2017.
 */
public class BindCommand extends Command {

    public static Setting<Boolean> modifiersEnabled = SettingBuilder.register(Settings.b("modifiersEnabled", false), "binds");

    public BindCommand() {
        super("bind", new ChunkBuilder()
                .append("[module]|modifiers", true, new ModuleParser())
                .append("[key]|[on|off]", true)
                .build()
        );
        setDescription("Binds a module to a key, or allows you to change modifier options");
    }

    @Override
    public void call(String[] args) {
        if (args.length == 1) {
            Command.sendChatMessage("Please specify a module.");
            return;
        }

        String module = args[0];
        String rkey = args[1];

        if (module.equalsIgnoreCase("modifiers")) {
            if (rkey == null) {
                sendChatMessage("Expected: on or off");
                return;
            }

            if (rkey.equalsIgnoreCase("on")) {
                modifiersEnabled.setValue(true);
                sendChatMessage("Turned modifiers on.");
            } else if (rkey.equalsIgnoreCase("off")) {
                modifiersEnabled.setValue(false);
                sendChatMessage("Turned modifiers off.");
            } else {
                sendChatMessage("Expected: on or off");
            }
            return;
        }

        try {
            Module m = MODULE_MANAGER.getModule(module);
            if (rkey == null) {
                sendChatMessage(m.getName() + " is bound to &b" + m.getBindName());
                return;
            }
            int key = Wrapper.getKey(rkey);
            if (rkey.equalsIgnoreCase("none")) {
                key = -1;
            }
            if (key == 0) {
                sendChatMessage("Unknown key '" + rkey + "'!");
                return;
            }
            m.getBind().setKey(key);
            sendChatMessage("Bind for &b" + m.getName() + "&r set to &b" + rkey.toUpperCase());
        } catch (ModuleManager.ModuleNotFoundException x) {
            sendChatMessage("Unknown module '" + module + "'!");
            return;
        }
    }
}
