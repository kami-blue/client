package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * @author d1gress/Qther
 * Created by d1gress/Qther on 4/12/2019.
 */
public class NBTCommand extends Command {

    public NBTCommand() {
        super("nbt", new ChunkBuilder()
                .append("action", true, new EnumParser(new String[]{"get", "copy", "wipe"}))
                .build());
        setDescription("Does NBT related stuff (get, copy, set)");
    }

    Minecraft mc = Minecraft.getMinecraft();
    private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    StringSelection nbt;

    @Override
    public void call(String[] args) {
        if(args[0].isEmpty()) {
            sendErrorMessage("Invalid Syntax!");
            return;
        }
        ItemStack item = mc.player.inventory.getCurrentItem();

        if (args[0].equalsIgnoreCase("get")) {
            if (item.getTagCompound() != null) {
                sendChatMessage("&6&lNBT:\n" + item.getTagCompound() + "");
            } else sendErrorMessage("No NBT on " + item.getDisplayName());
        } else if (args[0].equalsIgnoreCase("copy")) {
            if (item.getTagCompound() != null) {
                nbt = new StringSelection(item.getTagCompound() + "");
                clipboard.setContents(nbt, nbt);
                sendChatMessage("&6Copied\n&f" + (item.getTagCompound() + "\n") + "&6to clipboard.");
            } else sendErrorMessage("No NBT on " + item.getDisplayName());
        } else if (args[0].equalsIgnoreCase("wipe")) {
            sendChatMessage("&6Wiped\n&f" + (item.getTagCompound() + "\n") + "&6from " + item.getDisplayName() + ".");
            item.setTagCompound(new NBTTagCompound());
        }
    }
}
