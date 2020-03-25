package me.zeroeightsix.kami.command;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.commands.BindCommand;
import me.zeroeightsix.kami.util.ClassFinder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class CommandManager {

    private ArrayList<Command> commands;

    public CommandManager() {
        commands = new ArrayList<>();

        Set<Class> classList = ClassFinder.findClasses(BindCommand.class.getPackage().getName(), Command.class);
        for (Class s : classList) {
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
        String[] args = removeElement(parts, 0);

        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) continue;
            args[i] = strip(args[i], "\"");
        }

        for (Command c : commands) {
            if (c.getLabel().equalsIgnoreCase(label) || c.getAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(label))) {
                c.call(parts);
                return;
            }
        }

        Command.sendChatMessage("&7Unknown command. try '&f" + Command.getCommandPrefix() + "commands&7' for a list of commands.");
    }

    public static String[] removeElement(String[] input, int indexToDelete) {
        List result = new LinkedList();

        for (int i = 0; i < input.length; i++) {
            if (i != indexToDelete) result.add(input[i]);
        }

        return (String[]) result.toArray(input);
    }


    private static String strip(String str, String key) {
        if (str.startsWith(key) && str.endsWith(key)) return str.substring(key.length(), str.length() - key.length());
        return str;
    }

    public Command getCommandByLabel(String commandLabel) {
        for (Command c : commands) {
            if (c.getLabel().equals(commandLabel)) return c;
        }
        return null;
    }

    public ArrayList<Command> getCommands() {
        return commands;
    }

}
