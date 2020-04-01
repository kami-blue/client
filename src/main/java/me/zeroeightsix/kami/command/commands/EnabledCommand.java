package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.module.Module;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

/**
 * @author S-B99
 * Updated by S-B99 on 07/02/20
 */
public class EnabledCommand extends Command {
    public EnabledCommand() {
        super("enabled", new ChunkBuilder().append("filter").build());
        setDescription("Prints enabled modules");
    }

    @Override
    public void call(String[] args) {
        AtomicReference<String> enabled = new AtomicReference<>("");
        List<Module> mods = new ArrayList<>(MODULE_MANAGER.getModules());

        String f = "";
        if (args[0] != null) f = "(filter: " + args[0] + ")";

        mods.forEach(module -> {
            if (args[0] == null) {
                if (module.isEnabled()) {
                    enabled.set(enabled + module.getName() + ", ");
                }
            } else {
                if (module.isEnabled() && Pattern.compile(args[0], Pattern.CASE_INSENSITIVE).matcher(module.getName()).find()) {
                    enabled.set(enabled + module.getName() + ", ");
                }
            }
        });

        enabled.set(StringUtils.chop(StringUtils.chop(String.valueOf(enabled)))); // this looks horrible but I don't know how else to do it sorry
        Command.sendChatMessage("Enabled modules: " + f + "\n" + TextFormatting.GRAY + enabled);
    }

}
