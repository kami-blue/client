package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.render.XRay;
import net.minecraft.block.Block;

/**
 * Created by 20kdc on 17/02/2020.
 * Updated by S-B99 on 17/02/20
 * Note for anybody using this in a development environment: THIS DOES NOT WORK. It will lag and the texture will break
 */
public class XRayCommand extends Command {
    public XRayCommand() {
        super("xray", new ChunkBuilder().append("help").append("+block|-block|=block").append("list|defaults|clear|invert").build(), "wallhack", "wireframe");
        setDescription("Allows you to add or remove blocks from the &7xray &8module");
    }

    @Override
    public void call(String[] args) {
        XRay xr = (XRay) ModuleManager.getModuleByName("XRay");
        if (xr == null) {
            Command.sendErrorMessage("&cThe XRay module is not available for some reason. Make sure the name you're calling is correct and that you have the module installed!!");
            return;
        }
        if (!xr.isEnabled()) {
            Command.sendWarningMessage("&6Warning: The XRay module is not enabled!");
            Command.sendWarningMessage("These commands will still have effect, but will not visibly do anything.");
        }
        for (String s : args) {
            if (s == null)
                continue;
            if (s.equalsIgnoreCase("help")) {
                Command.sendChatMessage("The XRay module has a list of blocks");
                Command.sendChatMessage("Normally, the XRay module hides these blocks");
                Command.sendChatMessage("When the Invert setting is on, the XRay only shows these blocks");
                Command.sendChatMessage("This command is a convenient way to quickly edit the list");
                Command.sendChatMessage("Available options: \n" +
                        "+block: Adds a block to the list\n" +
                        "-block: Removes a block from the list\n" +
                        "=block: Changes the list to only that block\n" +
                        "list: Prints the list of selected blocks\n" +
                        "defaults: Resets the list to the default list\n" +
                        "clear: Removes all blocks from the XRay block list\n" +
                        "invert: Quickly toggles the invert setting");
            } else if (s.equalsIgnoreCase("clear")) {
                xr.extClear();
                Command.sendWarningMessage("Cleared the XRay block list");
            } else if (s.equalsIgnoreCase("defaults")) {
                xr.extDefaults();
                Command.sendChatMessage("Reset the XRay block list to default");
            } else if (s.equalsIgnoreCase("list")) {
                Command.sendChatMessage("\n" + xr.extGet());
            } else if (s.equalsIgnoreCase("invert")) {
                if (xr.invert.getValue()) {
                    xr.invert.setValue(false);
                    Command.sendChatMessage("Disabled XRay Invert");
                } else {
                    xr.invert.setValue(true);
                    Command.sendChatMessage("Enabled XRay Invert");
                }
            } else if (s.startsWith("=")) {
                String sT = s.replace("=" ,"");
                xr.extSet(sT);
                Command.sendChatMessage("Set the XRay block list to " + sT);
            } else if (s.startsWith("+") || s.startsWith("-")) {
                String name = s.substring(1);
                Block b = Block.getBlockFromName(name);
                if (b == null) {
                    Command.sendChatMessage("&cInvalid block name <" + name + ">");
                } else {
                    if (s.startsWith("+")) {
                        xr.extAdd(name);
                    } else {
                        xr.extRemove(name);
                    }
                }
            } else {
                Command.sendChatMessage("&cInvalid subcommand <" + s + ">");
            }
        }
    }
}
