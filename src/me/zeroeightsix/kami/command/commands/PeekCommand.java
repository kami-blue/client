// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.command.commands;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemShulkerBox;
import me.zeroeightsix.kami.util.Wrapper;
import me.zeroeightsix.kami.command.syntax.SyntaxChunk;
import net.minecraft.tileentity.TileEntityShulkerBox;
import me.zeroeightsix.kami.command.Command;

public class PeekCommand extends Command
{
    public static TileEntityShulkerBox sb;
    
    public PeekCommand() {
        super("peek", SyntaxChunk.EMPTY);
        this.setDescription("Look inside the contents of a shulker box without opening it");
    }
    
    @Override
    public void call(final String[] args) {
        final ItemStack is = Wrapper.getPlayer().field_71071_by.func_70448_g();
        if (is.func_77973_b() instanceof ItemShulkerBox) {
            final TileEntityShulkerBox entityBox = new TileEntityShulkerBox();
            entityBox.field_145854_h = ((ItemShulkerBox)is.func_77973_b()).func_179223_d();
            entityBox.func_145834_a(Wrapper.getWorld());
            entityBox.func_145839_a(is.func_77978_p().func_74775_l("BlockEntityTag"));
            PeekCommand.sb = entityBox;
        }
        else {
            Command.sendChatMessage("You aren't carrying a shulker box.");
        }
    }
}
