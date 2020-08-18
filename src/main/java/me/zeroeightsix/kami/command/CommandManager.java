package me.zeroeightsix.kami.command;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.commands.BindCommand;
import me.zeroeightsix.kami.util.ClassFinder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static me.zeroeightsix.kami.util.CommandUtil.runAliases;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;

public class CommandManager {

    private final ArrayList<Command> commands;

    public CommandManager() {
        commands = new ArrayList<>();

        for (Class<?> s : ClassFinder.findClasses(BindCommand.class.getPackage().getName(), Command.class)) {
            if (Command.class.isAssignableFrom(s)) {
                try {
                    Command command = (Command) s.getConstructor().newInstance();
                    commands.add(command);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Couldn't initiate command " + s.getSimpleName() + "! Err: " + e.getClass().getSimpleName() + ", message: " + e.getMessage());
                }
            }
        }
        KamiMod.log.info("Commands initialised");
    }

    public void callCommand(String command) {
        String[] parts = command.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Split by every space if it isn't surrounded by quotes

        String label = parts[0].contains(" ") ? parts[0].substring(parts[0].indexOf(" ")).substring(1) : parts[0].substring(1);
        String[] args = removeElement(parts);

        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) continue;
            args[i] = strip(args[i]);
        }

        for (Command c : commands) {
            if (c.getLabel().equalsIgnoreCase(label)) {
                c.call(parts);
                runAliases(c);
                return;
            } else if (c.getAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(label))) {
                c.call(parts);
                return;
            }
        }

        sendChatMessage("&7Unknown command. try '&f" + Command.getCommandPrefix() + "cmds&7' for a list of commands.");
    }

    private static String[] removeElement(String[] input) {
        List<String> result = new LinkedList<>();

        for (int i = 0; i < input.length; i++) {
            if (i != 0) result.add(input[i]);
        }

        return result.toArray(input);
    }


    private static String strip(String str) {
        if (str.startsWith("\"") && str.endsWith("\"")) return str.substring("\"".length(), str.length() - "\"".length());
        return str;
    }

    public ArrayList<Command> getCommands() {
        return commands;
    }

}
