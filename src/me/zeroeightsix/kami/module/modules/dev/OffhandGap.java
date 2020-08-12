// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.dev;

import java.util.function.ToIntFunction;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.client.gui.inventory.GuiContainer;
import me.zeroeightsix.kami.module.modules.combat.AutoTotem;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "OffhandGap", category = Category.DEV, description = "Auto Offhand Gapple")
public class OffhandGap extends Module
{
    private int gapples;
    private boolean moving;
    private boolean returnI;
    private Setting<Boolean> soft;
    private Setting<Boolean> totemOnDisable;
    private Setting<TotemMode> totemMode;
    
    public OffhandGap() {
        this.moving = false;
        this.returnI = false;
        this.soft = this.register(Settings.b("Soft", false));
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
        if (OffhandGap.mc.field_71462_r instanceof GuiContainer) {
            return;
        }
        if (this.returnI) {
            int t = -1;
            for (int i = 0; i < 45; ++i) {
                if (OffhandGap.mc.field_71439_g.field_71071_by.func_70301_a(i).field_190928_g) {
                    t = i;
                    break;
                }
            }
            if (t == -1) {
                return;
            }
            OffhandGap.mc.field_71442_b.func_187098_a(0, (t < 9) ? (t + 36) : t, 0, ClickType.PICKUP, (EntityPlayer)OffhandGap.mc.field_71439_g);
            this.returnI = false;
        }
        this.gapples = OffhandGap.mc.field_71439_g.field_71071_by.field_70462_a.stream().filter(itemStack -> itemStack.func_77973_b() == Items.field_151153_ao).mapToInt(ItemStack::func_190916_E).sum();
        if (OffhandGap.mc.field_71439_g.func_184592_cb().func_77973_b() == Items.field_151153_ao) {
            ++this.gapples;
        }
        else {
            if (this.soft.getValue() && !OffhandGap.mc.field_71439_g.func_184592_cb().field_190928_g) {
                return;
            }
            if (this.moving) {
                OffhandGap.mc.field_71442_b.func_187098_a(0, 45, 0, ClickType.PICKUP, (EntityPlayer)OffhandGap.mc.field_71439_g);
                this.moving = false;
                if (!OffhandGap.mc.field_71439_g.field_71071_by.field_70457_g.func_190926_b()) {
                    this.returnI = true;
                }
                return;
            }
            if (OffhandGap.mc.field_71439_g.field_71071_by.field_70457_g.func_190926_b()) {
                if (this.gapples == 0) {
                    return;
                }
                int t = -1;
                for (int i = 0; i < 45; ++i) {
                    if (OffhandGap.mc.field_71439_g.field_71071_by.func_70301_a(i).func_77973_b() == Items.field_151153_ao) {
                        t = i;
                        break;
                    }
                }
                if (t == -1) {
                    return;
                }
                OffhandGap.mc.field_71442_b.func_187098_a(0, (t < 9) ? (t + 36) : t, 0, ClickType.PICKUP, (EntityPlayer)OffhandGap.mc.field_71439_g);
                this.moving = true;
            }
            else if (!this.soft.getValue()) {
                int t = -1;
                for (int i = 0; i < 45; ++i) {
                    if (OffhandGap.mc.field_71439_g.field_71071_by.func_70301_a(i).field_190928_g) {
                        t = i;
                        break;
                    }
                }
                if (t == -1) {
                    return;
                }
                OffhandGap.mc.field_71442_b.func_187098_a(0, (t < 9) ? (t + 36) : t, 0, ClickType.PICKUP, (EntityPlayer)OffhandGap.mc.field_71439_g);
            }
        }
    }
    
    @Override
    public String getHudInfo() {
        return String.valueOf(this.gapples);
    }
    
    private enum TotemMode
    {
        KAMI, 
        ASIMOV;
    }
}
