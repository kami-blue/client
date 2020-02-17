package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.render.XRay;
import net.minecraft.block.Block;

/**
 * Created by 20kdc on 17/02/2020.
 */
public class XRayCommand extends Command {
    public XRayCommand() {
        super("xray", new ChunkBuilder().append("subcommand").build());
        setDescription("Has a set of sub-commands to control the XRay module.");
    }

    @Override
    public void call(String[] args) {
        XRay xr = (XRay) ModuleManager.getModuleByName("XRay");
        if (xr == null) {
            Command.sendChatMessage("&cThe XRay module is not available for some reason.");
            return;
        }
        if (!xr.isEnabled()) {
            Command.sendChatMessage("&cWarning: The XRay module is not enabled.");
            Command.sendChatMessage("&cThese commands will still have effects, but will not visibly do anything.");
        }
        for (String s : args) {
            if (s == null)
                continue;
            if (s.equalsIgnoreCase("help")) {
                Command.sendChatMessage("The XRay module has a list of blocks.");
                Command.sendChatMessage("Normally, the XRay module hides these blocks.");
                Command.sendChatMessage("When the Invert setting is on, the XRay only shows these blocks.");
                Command.sendChatMessage("This command is a convenient way to quickly edit the list.");
                Command.sendChatMessage("XRay Subcommands:");
                Command.sendChatMessage(".xray help : Shows this text.");
                Command.sendChatMessage(".xray clear : Removes all blocks from the XRay block list.");
                Command.sendChatMessage(".xray show : Shows the list.");
                Command.sendChatMessage(".xray +<block> : Adds a block to the list.");
                Command.sendChatMessage(".xray -<block> : Removes a block from the list.");
            } else if (s.equalsIgnoreCase("clear")) {
                xr.extClear();
                Command.sendChatMessage("Cleared the XRay block list.");
            } else if (s.equalsIgnoreCase("show")) {
                Command.sendChatMessage(xr.extGet());
            } else if (s.startsWith("+") || s.startsWith("-")) {
                String name = s.substring(1);
                Block b = Block.getBlockFromName(name);
                if (b == null) {
                    Command.sendChatMessage("&cInvalid block name " + name + ".");
                } else {
                    if (s.startsWith("+")) {
                        xr.extAdd(name);
                    } else {
                        xr.extRemove(name);
                    }
                }
            } else {
                Command.sendChatMessage("&cInvalid subcommand " + s + ".");
            }
        }
    }
}
