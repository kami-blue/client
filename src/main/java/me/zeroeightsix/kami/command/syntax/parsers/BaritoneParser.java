package me.zeroeightsix.kami.command.syntax.parsers;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import baritone.api.command.argument.ICommandArgument;
import baritone.api.command.exception.CommandNotEnoughArgumentsException;
import baritone.api.command.helpers.TabCompleteHelper;
import baritone.api.command.manager.ICommandManager;
import baritone.api.utils.SettingsUtil;
import baritone.command.argument.ArgConsumer;
import baritone.command.argument.CommandArguments;
import me.zeroeightsix.kami.command.syntax.SyntaxChunk;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BaritoneParser extends AbstractParser {
    private Settings settings = null;
    private ICommandManager manager = null;

    @Override
    public String getChunk(SyntaxChunk[] chunks, SyntaxChunk thisChunk, String[] values, String command) {
        if (command == null)
            return getDefaultChunk(thisChunk);

        List<String> completeList = tabComplete(command).sorted().collect(Collectors.toList());

        try {
            return completeList.get(0).substring(command.length());
        } catch (IndexOutOfBoundsException ignored) {} // happens if there's no autocomplete
        return "";
    }

    /**
     * This method is a part of Baritone, licensed under LGPLv3. You can view the license online here:
     * https://github.com/cabaletta/baritone/blob/master/LICENSE
     * @param msg first argument of command
     * @return stream of all the settings for the argument
     */
    public Stream<String> tabComplete(String msg) {
        try {
            List<ICommandArgument> args = CommandArguments.from(msg, true);
            ArgConsumer argc = new ArgConsumer(getManager(), args);
            if (argc.hasAtMost(2)) {
                if (argc.hasExactly(1)) {
                    return new TabCompleteHelper()
                            .addCommands(getManager())
                            .addSettings()
                            .filterPrefix(argc.getString())
                            .stream();
                }
                Settings.Setting setting = getSettings().byLowerName.get(argc.getString().toLowerCase(Locale.US));
                if (setting != null) {
                    if (setting.getValueClass() == Boolean.class) {
                        TabCompleteHelper helper = new TabCompleteHelper();
                        if ((Boolean) setting.value) {
                            helper.append("true", "false");
                        } else {
                            helper.append("false", "true");
                        }
                        return helper.filterPrefix(argc.getString()).stream();
                    } else {
                        return Stream.of(SettingsUtil.settingValueToString(setting));
                    }
                }
            }
            return getManager().tabComplete(msg);
        } catch (CommandNotEnoughArgumentsException ignored) { // Shouldn't happen, the operation is safe
            return Stream.empty();
        }
    }

    private Settings getSettings() {
        try {
            this.settings = BaritoneAPI.getSettings();
        } catch (NullPointerException ignored) {} // this will null while loading minecraft, that's fine
        return this.settings;
    }

    private ICommandManager getManager() {
        try {
            this.manager = BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager();
        } catch (NullPointerException ignored) {} // see above
        return this.manager;
    }
}
