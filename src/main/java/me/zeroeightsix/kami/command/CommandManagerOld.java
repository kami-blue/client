package me.zeroeightsix.kami.command;

import me.zeroeightsix.kami.KamiMod;
import org.kamiblue.commons.utils.ClassUtils;

import java.util.ArrayList;
import java.util.List;

import static me.zeroeightsix.kami.util.CommandUtil.runAliases;
import static me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage;

public class CommandManagerOld {

    private final ArrayList<CommandOld> commands;

    public CommandManagerOld() {
        commands = new ArrayList<>();
        List<Class<? extends CommandOld>> classes = ClassUtils.INSTANCE.findClasses("me.zeroeightsix.kami.command.commands", CommandOld.class);

        for (Class<? extends CommandOld> s : classes) {
            try {
                CommandOld command = s.getConstructor().newInstance();
                commands.add(command);
            } catch (Exception e) {
                e.printStackTrace();
                KamiMod.LOG.error("Couldn't initiate command " + s.getSimpleName() + "! Err: " + e.getClass().getSimpleName() + ", message: " + e.getMessage());
            }
        }
        KamiMod.LOG.info("Commands initialised");
    }

    public void callCommand(String command) {
        String[] parts = command.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Split by every space if it isn't surrounded by quotes

        String label = parts[0].contains(" ") ? parts[0].substring(parts[0].indexOf(" ")).substring(1) : parts[0].substring(1);
        String[] args = removeElement(parts, 0);

        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) continue;
            args[i] = strip(args[i], "\"");
        }

        for (CommandOld c : commands) {
            if (c.getLabel().equalsIgnoreCase(label)) {
                c.call(parts);
                runAliases(c);
                return;
            } else if (c.getAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(label))) {
                c.call(parts);
                return;
            }
        }

        sendChatMessage("&7Unknown command. try '&f" + CommandOld.getCommandPrefix() + "cmds&7' for a list of commands.");
    }

    private static String[] removeElement(String[] input, int indexToDelete) {
        List<String> result = new ArrayList<>();

        for (int i = 0; i < input.length; i++) {
            if (i != indexToDelete) result.add(input[i]);
        }

        return result.toArray(input);
    }

    private static String strip(String str, String key) {
        if (str.startsWith(key) && str.endsWith(key)) return str.substring(key.length(), str.length() - key.length());
        return str;
    }

    public ArrayList<CommandOld> getCommands() {
        return commands;
    }

}
