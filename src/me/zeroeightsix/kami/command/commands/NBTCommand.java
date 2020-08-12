// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.command.commands;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.Toolkit;
import me.zeroeightsix.kami.command.syntax.SyntaxParser;
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import net.minecraft.client.Minecraft;
import me.zeroeightsix.kami.command.Command;

public class NBTCommand extends Command
{
    Minecraft mc;
    private final Clipboard clipboard;
    StringSelection nbt;
    
    public NBTCommand() {
        super("nbt", new ChunkBuilder().append("action", true, new EnumParser(new String[] { "get", "copy", "wipe" })).build());
        this.mc = Minecraft.func_71410_x();
        this.clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        this.setDescription("Does NBT related stuff (get, copy, set)");
    }
    
    @Override
    public void call(final String[] args) {
        if (args[0].isEmpty()) {
            Command.sendErrorMessage("Invalid Syntax!");
            return;
        }
        final ItemStack item = this.mc.field_71439_g.field_71071_by.func_70448_g();
        if (args[0].equalsIgnoreCase("get")) {
            if (item.func_77978_p() != null) {
                Command.sendChatMessage("&4&lNBT: " + item.func_77978_p() + "");
            }
            else {
                Command.sendErrorMessage("No NBT on " + item.func_82833_r());
            }
        }
        else if (args[0].equalsIgnoreCase("copy")) {
            if (item.func_77978_p() != null) {
                this.nbt = new StringSelection(item.func_77978_p() + "");
                this.clipboard.setContents(this.nbt, this.nbt);
                Command.sendChatMessage("&4Copied &f" + item.func_77978_p() + "\n" + "&4to clipboard.");
            }
            else {
                Command.sendErrorMessage("No NBT on " + item.func_82833_r());
            }
        }
        else if (args[0].equalsIgnoreCase("wipe")) {
            Command.sendChatMessage("&4Wiped &f" + item.func_77978_p() + "\n" + "&4from " + item.func_82833_r() + ".");
            item.func_77982_d(new NBTTagCompound());
        }
    }
}
