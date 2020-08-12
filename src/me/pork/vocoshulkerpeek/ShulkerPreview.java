// 
// Decompiled by Procyon v0.5.36
// 

package me.pork.vocoshulkerpeek;

import net.minecraft.inventory.IInventory;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.module.ModuleManager;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.entity.item.EntityItem;

public class ShulkerPreview
{
    public static int metadataTicks;
    public static int guiTicks;
    public static EntityItem drop;
    public static InventoryBasic toOpen;
    
    @SubscribeEvent
    public void onEntitySpawn(final EntityJoinWorldEvent event) {
        final Entity entity = event.getEntity();
        if (entity instanceof EntityItem) {
            ShulkerPreview.drop = (EntityItem)entity;
            ShulkerPreview.metadataTicks = 0;
        }
    }
    
    @SubscribeEvent
    public void onTick(final TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && ShulkerPreview.metadataTicks > -1) {
            ++ShulkerPreview.metadataTicks;
        }
        if (event.phase == TickEvent.Phase.END && ShulkerPreview.guiTicks > -1) {
            ++ShulkerPreview.guiTicks;
        }
        if (ShulkerPreview.metadataTicks == 20) {
            if (Minecraft.func_71410_x().field_71439_g == null) {
                return;
            }
            ShulkerPreview.metadataTicks = -1;
            if (ShulkerPreview.drop.func_92059_d().func_77973_b() instanceof ItemShulkerBox && ModuleManager.getModuleByName("ShulkerBypass").isEnabled()) {
                Command.sendChatMessage("[ShulkerBypass] New shulker found! use /peek to view its content");
                VocoShulkerPeek.shulker = ShulkerPreview.drop.func_92059_d();
            }
        }
        if (ShulkerPreview.guiTicks == 20) {
            ShulkerPreview.guiTicks = -1;
            VocoShulkerPeek.mc.field_71439_g.func_71007_a((IInventory)ShulkerPreview.toOpen);
        }
    }
    
    static {
        ShulkerPreview.metadataTicks = -1;
        ShulkerPreview.guiTicks = -1;
    }
}
