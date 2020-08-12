// 
// Decompiled by Procyon v0.5.36
// 

package me.pork.vocoshulkerpeek;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.server.MinecraftServer;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.client.IClientCommand;
import net.minecraft.command.CommandBase;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.module.ModuleManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.command.ICommand;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;

@Mod(modid = "vocoshulkerpeek", name = "Peek Bypass for KAMI Blue", version = "1.1", acceptedMinecraftVersions = "[1.12.2]")
public class VocoShulkerPeek
{
    public static final String MOD_ID = "vocoshulkerpeek";
    public static final String MOD_NAME = "VocoShulkerPeek";
    public static final String VERSION = "1.0";
    public static ItemStack shulker;
    public static Minecraft mc;
    
    @Mod.EventHandler
    public void postinit(final FMLPostInitializationEvent event) {
        ClientCommandHandler.instance.func_71560_a((ICommand)new PeekCommand());
        MinecraftForge.EVENT_BUS.register((Object)new ShulkerPreview());
    }
    
    public static NBTTagCompound getShulkerNBT(final ItemStack stack) {
        if (VocoShulkerPeek.mc.field_71439_g == null) {
            return null;
        }
        final NBTTagCompound compound = stack.func_77978_p();
        if (compound != null && compound.func_150297_b("BlockEntityTag", 10)) {
            final NBTTagCompound tags = compound.func_74775_l("BlockEntityTag");
            if (ModuleManager.getModuleByName("ShulkerBypass").isEnabled()) {
                if (tags.func_150297_b("Items", 9)) {
                    return tags;
                }
                Command.sendWarningMessage("[ShulkerBypass] Shulker is empty!");
            }
        }
        return null;
    }
    
    static {
        VocoShulkerPeek.shulker = ItemStack.field_190927_a;
        VocoShulkerPeek.mc = Minecraft.func_71410_x();
    }
    
    public static class PeekCommand extends CommandBase implements IClientCommand
    {
        public boolean allowUsageWithoutPrefix(final ICommandSender sender, final String message) {
            return false;
        }
        
        public String func_71517_b() {
            return "peek";
        }
        
        public String func_71518_a(final ICommandSender sender) {
            return null;
        }
        
        public void func_184881_a(final MinecraftServer server, final ICommandSender sender, final String[] args) {
            if (VocoShulkerPeek.mc.field_71439_g != null && ModuleManager.getModuleByName("ShulkerBypass").isEnabled()) {
                if (!VocoShulkerPeek.shulker.func_190926_b()) {
                    final NBTTagCompound shulkerNBT = VocoShulkerPeek.getShulkerNBT(VocoShulkerPeek.shulker);
                    if (shulkerNBT != null) {
                        final TileEntityShulkerBox fakeShulker = new TileEntityShulkerBox();
                        fakeShulker.func_190586_e(shulkerNBT);
                        String customName = "container.shulkerBox";
                        boolean hasCustomName = false;
                        if (shulkerNBT.func_150297_b("CustomName", 8)) {
                            customName = shulkerNBT.func_74779_i("CustomName");
                            hasCustomName = true;
                        }
                        final InventoryBasic inv = new InventoryBasic(customName, hasCustomName, 27);
                        for (int i = 0; i < 27; ++i) {
                            inv.func_70299_a(i, fakeShulker.func_70301_a(i));
                        }
                        ShulkerPreview.toOpen = inv;
                        ShulkerPreview.guiTicks = 0;
                    }
                }
                else {
                    Command.sendChatMessage("[ShulkerBypass] No shulker detected! please drop and pickup your shulker.");
                }
            }
        }
        
        public boolean func_184882_a(final MinecraftServer server, final ICommandSender sender) {
            return true;
        }
    }
}
