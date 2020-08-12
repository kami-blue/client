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
import net.minecraft.client.gui.inventory.GuiContainer;
import me.zeroeightsix.kami.module.modules.combat.AutoTotem;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "OffhandGapAsimov", category = Category.DEV, description = "Auto Offhand Gapple")
public class OffhandGapAsimov extends Module
{
    private int numOfGaps;
    private int preferredGapSlot;
    private Setting<Boolean> totemOnDisable;
    private Setting<TotemMode> totemMode;
    
    public OffhandGapAsimov() {
        this.totemOnDisable = this.register(Settings.b("TotemOnDisable", true));
        this.totemMode = this.register((Setting<TotemMode>)Settings.enumBuilder(TotemMode.class).withName("TotemMode").withValue(TotemMode.KAMI).withVisibility(v -> this.totemOnDisable.getValue()).build());
    }
    
    public void onEnable() {
        if (ModuleManager.getModuleByName("AutoTotem").isEnabled()) {
            ModuleManager.getModuleByName("AutoTotem").disable();
        }
        if (ModuleManager.getModuleByName("AutoTotemDev").isEnabled()) {
            ModuleManager.getModuleByName("AutoTotemDev").disable();
        }
    }
    
    public void onDisable() {
        if (!this.totemOnDisable.getValue()) {
            return;
        }
        if (this.totemMode.getValue().equals(TotemMode.KAMI)) {
            final AutoTotem autoTotem = (AutoTotem)ModuleManager.getModuleByName("AutoTotem");
            autoTotem.disableSoft();
            if (autoTotem.isDisabled()) {
                autoTotem.enable();
            }
        }
        if (this.totemMode.getValue().equals(TotemMode.ASIMOV)) {
            final AutoTotemDev autoTotemDev = (AutoTotemDev)ModuleManager.getModuleByName("AutoTotemDev");
            autoTotemDev.disableSoft();
            if (autoTotemDev.isDisabled()) {
                autoTotemDev.enable();
            }
        }
    }
    
    @Override
    public void onUpdate() {
        if (OffhandGapAsimov.mc.field_71439_g == null) {
            return;
        }
        if (OffhandGapAsimov.mc.field_71462_r instanceof GuiContainer) {
            return;
        }
        if (!this.findGaps()) {
            this.disable();
            return;
        }
        if (!OffhandGapAsimov.mc.field_71439_g.func_184592_cb().func_77973_b().equals(Items.field_151153_ao)) {
            boolean offhandEmptyPreSwitch = false;
            if (OffhandGapAsimov.mc.field_71439_g.func_184592_cb().func_77973_b().equals(Items.field_190931_a)) {
                offhandEmptyPreSwitch = true;
            }
            OffhandGapAsimov.mc.field_71442_b.func_187098_a(0, this.preferredGapSlot, 0, ClickType.PICKUP, (EntityPlayer)OffhandGapAsimov.mc.field_71439_g);
            OffhandGapAsimov.mc.field_71442_b.func_187098_a(0, 45, 0, ClickType.PICKUP, (EntityPlayer)OffhandGapAsimov.mc.field_71439_g);
            if (!offhandEmptyPreSwitch) {
                OffhandGapAsimov.mc.field_71442_b.func_187098_a(0, this.preferredGapSlot, 0, ClickType.PICKUP, (EntityPlayer)OffhandGapAsimov.mc.field_71439_g);
            }
            OffhandGapAsimov.mc.field_71442_b.func_78765_e();
        }
    }
    
    private boolean findGaps() {
        this.numOfGaps = 0;
        final AtomicInteger preferredGapSlotStackSize = new AtomicInteger();
        preferredGapSlotStackSize.set(Integer.MIN_VALUE);
        int numOfGapsInStack;
        final AtomicInteger atomicInteger;
        getInventoryAndHotbarSlots().forEach((slotKey, slotValue) -> {
            numOfGapsInStack = 0;
            if (slotValue.func_77973_b().equals(Items.field_151153_ao)) {
                numOfGapsInStack = slotValue.func_190916_E();
                if (atomicInteger.get() < numOfGapsInStack) {
                    atomicInteger.set(numOfGapsInStack);
                    this.preferredGapSlot = slotKey;
                }
            }
            this.numOfGaps += numOfGapsInStack;
            return;
        });
        if (OffhandGapAsimov.mc.field_71439_g.func_184592_cb().func_77973_b().equals(Items.field_151153_ao)) {
            this.numOfGaps += OffhandGapAsimov.mc.field_71439_g.func_184592_cb().func_190916_E();
        }
        return this.numOfGaps != 0;
    }
    
    private static Map<Integer, ItemStack> getInventoryAndHotbarSlots() {
        return getInventorySlots(9, 44);
    }
    
    private static Map<Integer, ItemStack> getInventorySlots(int current, final int last) {
        final Map<Integer, ItemStack> fullInventorySlots = new HashMap<Integer, ItemStack>();
        while (current <= last) {
            fullInventorySlots.put(current, (ItemStack)OffhandGapAsimov.mc.field_71439_g.field_71069_bz.func_75138_a().get(current));
            ++current;
        }
        return fullInventorySlots;
    }
    
    private enum TotemMode
    {
        KAMI, 
        ASIMOV;
    }
}
