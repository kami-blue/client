// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.command;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Set;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.util.ClassFinder;
import me.zeroeightsix.kami.command.commands.BindCommand;
import java.util.ArrayList;

public class CommandManager
{
    private ArrayList<Command> commands;
    
    public CommandManager() {
        this.commands = new ArrayList<Command>();
        final Set<Class> classList = ClassFinder.findClasses(BindCommand.class.getPackage().getName(), Command.class);
        for (final Class s : classList) {
            if (Command.class.isAssignableFrom(s)) {
                try {
                    final Command command = s.getConstructor((Class<?>[])new Class[0]).newInstance(new Object[0]);
                    this.commands.add(command);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Couldn't initiate command " + s.getSimpleName() + "! Err: " + e.getClass().getSimpleName() + ", message: " + e.getMessage());
                }
            }
        }
        KamiMod.log.info("Commands initialised");
    }
    
    public void callCommand(final String command) {
        final String[] parts = command.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        final String label = parts[0].substring(1);
        final String[] args = removeElement(parts, 0);
        for (int i = 0; i < args.length; ++i) {
            if (args[i] != null) {
                args[i] = strip(args[i], "\"");
            }
        }
        for (final Command c : this.commands) {
            if (c.getLabel().equalsIgnoreCase(label)) {
                c.call(parts);
                return;
            }
        }
        Command.sendChatMessage("Unknown command. try 'commands' for a list of commands.");
    }
    
    public static String[] removeElement(final String[] input, final int indexToDelete) {
        final List result = new LinkedList();
        for (int i = 0; i < input.length; ++i) {
            if (i != indexToDelete) {
                result.add(input[i]);
            }
        }
        return result.toArray(input);
    }
    
    private static String strip(final String str, final String key) {
        if (str.startsWith(key) && str.endsWith(key)) {
            return str.substring(key.length(), str.length() - key.length());
        }
        return str;
    }
    
    public Command getCommandByLabel(final String commandLabel) {
        for (final Command c : this.commands) {
            if (c.getLabel().equals(commandLabel)) {
                return c;
            }
        }
        return null;
    }
    
    public ArrayList<Command> getCommands() {
        return this.commands;
    }
}
