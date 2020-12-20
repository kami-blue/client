package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser;
import me.zeroeightsix.kami.command.syntax.parsers.ModuleParser;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.ISettingUnknown;
import me.zeroeightsix.kami.setting.Setting;

import java.util.Optional;
import java.util.stream.Collectors;

import static me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage;
import static me.zeroeightsix.kami.util.text.MessageSendHelper.sendStringChatMessage;

/**
 * Created by 086 on 18/11/2017.
 * Updated by l1ving on 24/02/20
 */
public class SetCommand extends Command {

    public SetCommand() {
        super("set", new ChunkBuilder()
                .append("module", true, new ModuleParser())
                .append("setting", true)
                .append("set", true, new EnumParser(new String[]{"value", "toggle"}))
                .build());
        setDescription("Change the setting of a certain module");
    }

    @Override
    public void call(String[] args) {
        if (args[0] == null) {
            sendChatMessage("Please specify a module!");
            return;
        }

        Module m = ModuleManager.getModule(args[0]);
        if (m == null) {
            sendChatMessage("Unknown module &b" + args[0] + "&r!");
            return;
        }

        if (args[1] == null) {
            String settings = m.getFullSettingList().stream().map(Setting::getName).collect(Collectors.joining(", "));
            if (settings.isEmpty())
                sendChatMessage("Module &b" + m.getName().getValue() + "&r has no settings.");
            else {
                sendStringChatMessage(new String[]{
                        "Please specify a setting! Choose one of the following:", settings
                });
            }
            return;
        }

        Optional<Setting<?>> optionalSetting = m.getFullSettingList().stream().filter(setting1 -> setting1.getName().equalsIgnoreCase(args[1])).findFirst();
        if (!optionalSetting.isPresent()) {
            sendChatMessage("Unknown setting &b" + args[1] + "&r in &b" + m.getName().getValue() + "&r!");
            return;
        }

        ISettingUnknown setting = optionalSetting.get();

        if (args[2] == null) {
            sendChatMessage("&b" + setting.getName() + "&r is a &3" + setting.getValueClass().getSimpleName() + "&r. Its current value is &3" + setting.toString());
            return;
        }

        try {
            String arg2 = args[2];
            if (setting.getClass().getSimpleName().equals("EnumSetting")) {
                arg2 = arg2.toUpperCase();
            }
            /* PLEASE MAKE SURE TO USE PROPER NAMING WHEN USING ENUMS */ /* if you use improper lowercase letters it will *not* work with this command ie THIS_IS correct, this_is NOT ~l1ving */
            setting.setValueFromString(arg2, setting.getValueClass().getSimpleName().equals("Boolean"));
            sendChatMessage("Set &b" + setting.getName() + "&r to &3" + setting.toString() + "&r.");
        } catch (Exception e) {
            e.printStackTrace();
            sendChatMessage("Unable to set value! &6" + e.getMessage());
        }
    }
}
