// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.gui;

import net.minecraft.client.Minecraft;
import java.util.Iterator;
import me.zeroeightsix.kami.util.ColourHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import net.minecraft.client.renderer.RenderItem;
import me.zeroeightsix.kami.module.Module;

@Info(name = "ArmourHUD", category = Category.GUI)
public class ArmourHUD extends Module
{
    private static RenderItem itemRender;
    private Setting<Boolean> damage;
    
    public ArmourHUD() {
        this.damage = this.register(Settings.b("Damage", false));
    }
    
    @Override
    public void onRender() {
        GlStateManager.func_179098_w();
        final ScaledResolution resolution = new ScaledResolution(ArmourHUD.mc);
        final int i = resolution.func_78326_a() / 2;
        int iteration = 0;
        final int y = resolution.func_78328_b() - 55 - (ArmourHUD.mc.field_71439_g.func_70090_H() ? 10 : 0);
        for (final ItemStack is : ArmourHUD.mc.field_71439_g.field_71071_by.field_70460_b) {
            ++iteration;
            if (is.func_190926_b()) {
                continue;
            }
            final int x = i - 90 + (9 - iteration) * 20 + 2;
            GlStateManager.func_179126_j();
            ArmourHUD.itemRender.field_77023_b = 200.0f;
            ArmourHUD.itemRender.func_180450_b(is, x, y);
            ArmourHUD.itemRender.func_180453_a(ArmourHUD.mc.field_71466_p, is, x, y, "");
            ArmourHUD.itemRender.field_77023_b = 0.0f;
            GlStateManager.func_179098_w();
            GlStateManager.func_179140_f();
            GlStateManager.func_179097_i();
            final String s = (is.func_190916_E() > 1) ? (is.func_190916_E() + "") : "";
            ArmourHUD.mc.field_71466_p.func_175063_a(s, (float)(x + 19 - 2 - ArmourHUD.mc.field_71466_p.func_78256_a(s)), (float)(y + 9), 16777215);
            if (!this.damage.getValue()) {
                continue;
            }
            final float green = (is.func_77958_k() - (float)is.func_77952_i()) / is.func_77958_k();
            final float red = 1.0f - green;
            final int dmg = 100 - (int)(red * 100.0f);
            ArmourHUD.mc.field_71466_p.func_175063_a(dmg + "", (float)(x + 8 - ArmourHUD.mc.field_71466_p.func_78256_a(dmg + "") / 2), (float)(y - 11), ColourHolder.toHex((int)(red * 255.0f), (int)(green * 255.0f), 0));
        }
        GlStateManager.func_179126_j();
        GlStateManager.func_179140_f();
    }
    
    static {
        ArmourHUD.itemRender = Minecraft.func_71410_x().func_175599_af();
    }
}
