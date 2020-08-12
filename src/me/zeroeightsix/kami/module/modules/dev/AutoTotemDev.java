// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.dev;

import java.util.HashMap;
import net.minecraft.item.ItemStack;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.init.Items;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.inventory.GuiContainer;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "AutoTotemDev", category = Category.DEV, description = "Auto Totem")
public class AutoTotemDev extends Module
{
    private int numOfTotems;
    private int preferredTotemSlot;
    private Setting<Boolean> soft;
    private Setting<Boolean> pauseInContainers;
    private Setting<Boolean> pauseInInventory;
    
    public AutoTotemDev() {
        this.soft = this.register(Settings.b("Soft", false));
        this.pauseInContainers = this.register(Settings.b("PauseInContainers", true));
        this.pauseInInventory = this.register(Settings.b("PauseInInventory", true));
    }
    
    @Override
    public void onUpdate() {
        if (AutoTotemDev.mc.field_71439_g == null) {
            return;
        }
        if (!this.findTotems()) {
            return;
        }
        if (this.pauseInContainers.getValue() && AutoTotemDev.mc.field_71462_r instanceof GuiContainer && !(AutoTotemDev.mc.field_71462_r instanceof GuiInventory)) {
            return;
        }
        if (this.pauseInInventory.getValue() && AutoTotemDev.mc.field_71462_r instanceof GuiInventory && AutoTotemDev.mc.field_71462_r instanceof GuiInventory) {
            return;
        }
        if (this.soft.getValue()) {
            if (AutoTotemDev.mc.field_71439_g.func_184592_cb().func_77973_b().equals(Items.field_190931_a)) {
                AutoTotemDev.mc.field_71442_b.func_187098_a(0, this.preferredTotemSlot, 0, ClickType.PICKUP, (EntityPlayer)AutoTotemDev.mc.field_71439_g);
                AutoTotemDev.mc.field_71442_b.func_187098_a(0, 45, 0, ClickType.PICKUP, (EntityPlayer)AutoTotemDev.mc.field_71439_g);
                AutoTotemDev.mc.field_71442_b.func_78765_e();
            }
        }
        else if (!AutoTotemDev.mc.field_71439_g.func_184592_cb().func_77973_b().equals(Items.field_190929_cY)) {
            boolean offhandEmptyPreSwitch = false;
            if (AutoTotemDev.mc.field_71439_g.func_184592_cb().func_77973_b().equals(Items.field_190931_a)) {
                offhandEmptyPreSwitch = true;
            }
            AutoTotemDev.mc.field_71442_b.func_187098_a(0, this.preferredTotemSlot, 0, ClickType.PICKUP, (EntityPlayer)AutoTotemDev.mc.field_71439_g);
            AutoTotemDev.mc.field_71442_b.func_187098_a(0, 45, 0, ClickType.PICKUP, (EntityPlayer)AutoTotemDev.mc.field_71439_g);
            if (!offhandEmptyPreSwitch) {
                AutoTotemDev.mc.field_71442_b.func_187098_a(0, this.preferredTotemSlot, 0, ClickType.PICKUP, (EntityPlayer)AutoTotemDev.mc.field_71439_g);
            }
            AutoTotemDev.mc.field_71442_b.func_78765_e();
        }
    }
    
    private boolean findTotems() {
        this.numOfTotems = 0;
        final AtomicInteger preferredTotemSlotStackSize = new AtomicInteger();
        preferredTotemSlotStackSize.set(Integer.MIN_VALUE);
        int numOfTotemsInStack;
        final AtomicInteger atomicInteger;
        getInventoryAndHotbarSlots().forEach((slotKey, slotValue) -> {
            numOfTotemsInStack = 0;
            if (slotValue.func_77973_b().equals(Items.field_190929_cY)) {
                numOfTotemsInStack = slotValue.func_190916_E();
                if (atomicInteger.get() < numOfTotemsInStack) {
                    atomicInteger.set(numOfTotemsInStack);
                    this.preferredTotemSlot = slotKey;
                }
            }
            this.numOfTotems += numOfTotemsInStack;
            return;
        });
        if (AutoTotemDev.mc.field_71439_g.func_184592_cb().func_77973_b().equals(Items.field_190929_cY)) {
            this.numOfTotems += AutoTotemDev.mc.field_71439_g.func_184592_cb().func_190916_E();
        }
        return this.numOfTotems != 0;
    }
    
    private static Map<Integer, ItemStack> getInventoryAndHotbarSlots() {
        return getInventorySlots(9, 44);
    }
    
    private static Map<Integer, ItemStack> getInventorySlots(int current, final int last) {
        final Map<Integer, ItemStack> fullInventorySlots = new HashMap<Integer, ItemStack>();
        while (current <= last) {
            fullInventorySlots.put(current, (ItemStack)AutoTotemDev.mc.field_71439_g.field_71069_bz.func_75138_a().get(current));
            ++current;
        }
        return fullInventorySlots;
    }
    
    public void disableSoft() {
        this.soft.setValue(false);
    }
}
