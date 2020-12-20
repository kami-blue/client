package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser;
import me.zeroeightsix.kami.module.modules.render.XRay;
import net.minecraft.block.Block;

import static me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage;
import static me.zeroeightsix.kami.util.text.MessageSendHelper.sendWarningMessage;

public class XRayCommand extends Command {
    public XRayCommand() {
        super("xray", new ChunkBuilder().append("command", true, new EnumParser(new String[]{"help", "list", "+block", "-block", "=block", "defaults", "clear", "invert"})).build());
        setDescription("Allows you to add or remove blocks from the &fxray &7module");
    }

    @Override
    public void call(String[] args) {
        if (!XRay.INSTANCE.isEnabled()) {
            sendWarningMessage("&6Warning: The " + XRay.INSTANCE.getName().getValue() + " module is not enabled!");
            sendWarningMessage("These commands will still have effect, but will not visibly do anything.");
        }
        for (String s : args) {
            if (s == null)
                continue;
            if (s.equalsIgnoreCase("help")) {
                sendChatMessage("The " + XRay.INSTANCE.getName().getValue() + " module has a list of blocks");
                sendChatMessage("Normally, the " + XRay.INSTANCE.getName().getValue() + " module hides these blocks");
                sendChatMessage("When the Invert setting is on, the " + XRay.INSTANCE.getName().getValue() + " only shows these blocks");
                sendChatMessage("This command is a convenient way to quickly edit the list");
                sendChatMessage("Available options: \n" +
                        "+block: Adds a block to the list\n" +
                        "-block: Removes a block from the list\n" +
                        "=block: Changes the list to only that block\n" +
                        "list: Prints the list of selected blocks\n" +
                        "defaults: Resets the list to the default list\n" +
                        "clear: Removes all blocks from the " + XRay.INSTANCE.getName().getValue() + " block list\n" +
                        "invert: Quickly toggles the invert setting");
            } else if (s.equalsIgnoreCase("clear")) {
                XRay.INSTANCE.extClear();
                sendWarningMessage("Cleared the " + XRay.INSTANCE.getName().getValue() + " block list");
            } else if (s.equalsIgnoreCase("defaults")) {
                XRay.INSTANCE.extDefaults();
                sendChatMessage("Reset the " + XRay.INSTANCE.getName().getValue() + " block list to default");
            } else if (s.equalsIgnoreCase("list")) {
                sendChatMessage("\n" + XRay.INSTANCE.extGet());
            } else if (s.equalsIgnoreCase("invert")) {
                if (XRay.INSTANCE.invert.getValue()) {
                    XRay.INSTANCE.invert.setValue(false);
                    sendChatMessage("Disabled " + XRay.INSTANCE.getName().getValue() + " Invert");
                } else {
                    XRay.INSTANCE.invert.setValue(true);
                    sendChatMessage("Enabled " + XRay.INSTANCE.getName().getValue() + " Invert");
                }
            } else if (s.startsWith("=")) {
                String sT = s.replace("=", "");
                XRay.INSTANCE.extSet(sT);
                sendChatMessage("Set the " + XRay.INSTANCE.getName().getValue() + " block list to " + sT);
            } else if (s.startsWith("+") || s.startsWith("-")) {
                String name = s.substring(1);
                Block b = Block.getBlockFromName(name);
                if (b == null) {
                    sendChatMessage("&cInvalid block name <" + name + ">");
                } else {
                    if (s.startsWith("+")) {
                        sendChatMessage("Added <" + name + "> to the " + XRay.INSTANCE.getName().getValue() + " block list");
                        XRay.INSTANCE.extAdd(name);
                    } else {
                        sendChatMessage("Removed <" + name + "> from the " + XRay.INSTANCE.getName().getValue() + " block list");
                        XRay.INSTANCE.extRemove(name);
                    }
                }
            } else {
                sendChatMessage("&cInvalid subcommand <" + s + ">");
            }
        }
    }
}
