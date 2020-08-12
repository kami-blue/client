// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.command.commands;

import java.util.Arrays;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.command.syntax.SyntaxChunk;
import me.zeroeightsix.kami.command.Command;

public class HelpCommand extends Command
{
    private static final Subject[] subjects;
    private static String subjectsList;
    
    public HelpCommand() {
        super("help", new SyntaxChunk[0]);
        this.setDescription("Delivers help on certain subjects. Use &b" + Command.getCommandPrefix() + "help subjects&8 for a list.");
    }
    
    @Override
    public void call(final String[] args) {
        if (args[0] == null) {
            Command.sendStringChatMessage(new String[] { "AstraMod", "AstraMod", "commands&7 to view all available commands", "bind <module> <key>&7 to bind mods", "&7Press &r" + ModuleManager.getModuleByName("ClickGUI").getBindName() + "&7 to open GUI", "prefix <prefix>&r to change the command prefix.", "help <subjects:[subject]> &r for more help." });
        }
        else {
            final String subject3 = args[0];
            if (subject3.equals("subjects")) {
                Command.sendChatMessage("Subjects: " + HelpCommand.subjectsList);
            }
            else {
                final String[] names;
                final int length;
                int i = 0;
                String name;
                final String anotherString;
                final Subject subject4 = Arrays.stream(HelpCommand.subjects).filter(subject2 -> {
                    names = subject2.names;
                    length = names.length;
                    while (i < length) {
                        name = names[i];
                        if (name.equalsIgnoreCase(anotherString)) {
                            return true;
                        }
                        else {
                            ++i;
                        }
                    }
                    return false;
                }).findFirst().orElse(null);
                if (subject4 == null) {
                    Command.sendChatMessage("No help found for &b" + args[0]);
                    return;
                }
                Command.sendStringChatMessage(subject4.info);
            }
        }
    }
    
    static {
        subjects = new Subject[] { new Subject(new String[] { "type", "int", "boolean", "double", "float" }, new String[] { "Every module has a value, and that value is always of a certain &btype.\n", "These types are displayed in kami as the ones java use. They mean the following:", "&bboolean&r: Enabled or not. Values &3true/false", "&bfloat&r: A number with a decimal point", "&bdouble&r: Like a float, but a more accurate decimal point", "&bint&r: A number with no decimal point" }) };
        HelpCommand.subjectsList = "";
        for (final Subject subject : HelpCommand.subjects) {
            HelpCommand.subjectsList = HelpCommand.subjectsList + subject.names[0] + ", ";
        }
        HelpCommand.subjectsList = HelpCommand.subjectsList.substring(0, HelpCommand.subjectsList.length() - 2);
    }
    
    private static class Subject
    {
        String[] names;
        String[] info;
        
        public Subject(final String[] names, final String[] info) {
            this.names = names;
            this.info = info;
        }
    }
}
