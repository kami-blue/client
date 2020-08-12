// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.Minecraft;
import java.awt.Color;
import java.util.function.ToIntFunction;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import me.zeroeightsix.kami.util.ColourUtils;
import java.awt.Font;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.gui.font.CFontRenderer;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "PvPInfo", category = Category.RENDER)
public class PvPInfo extends Module
{
    private Setting<Float> x;
    private Setting<Float> y;
    private Setting<Boolean> rainbow;
    private Setting<Boolean> smooth;
    private Setting<Integer> red;
    private Setting<Integer> green;
    private Setting<Integer> blue;
    CFontRenderer cFontRenderer;
    
    public PvPInfo() {
        this.x = this.register(Settings.f("InfoX", 0.0f));
        this.y = this.register(Settings.f("InfoY", 200.0f));
        this.rainbow = this.register(Settings.b("Rainbow", false));
        this.smooth = this.register(Settings.b("Smooth Font", false));
        this.red = this.register((Setting<Integer>)Settings.integerBuilder("Red").withRange(0, 255).withValue(255).build());
        this.green = this.register((Setting<Integer>)Settings.integerBuilder("Green").withRange(0, 255).withValue(255).build());
        this.blue = this.register((Setting<Integer>)Settings.integerBuilder("Blue").withRange(0, 255).withValue(255).build());
        this.cFontRenderer = new CFontRenderer(new Font("Verdana", 0, 18), true, false);
    }
    
    @Override
    public void onRender() {
        float yCount = this.y.getValue();
        final int ared = this.red.getValue();
        final int bgreen = this.green.getValue();
        final int cblue = this.blue.getValue();
        int color;
        final int drgb = color = ColourUtils.toRGBA(ared, bgreen, cblue, 255);
        int totems = PvPInfo.mc.field_71439_g.field_71071_by.field_70462_a.stream().filter(itemStack -> itemStack.func_77973_b() == Items.field_190929_cY).mapToInt(ItemStack::func_190916_E).sum();
        if (PvPInfo.mc.field_71439_g.func_184592_cb().func_77973_b() == Items.field_190929_cY) {
            ++totems;
        }
        if (this.rainbow.getValue()) {
            final float[] hue = { System.currentTimeMillis() % 11520L / 11520.0f };
            final int rgb = Color.HSBtoRGB(hue[0], 1.0f, 1.0f);
            final int red = rgb >> 16 & 0xFF;
            final int green = rgb >> 8 & 0xFF;
            final int blue = rgb & 0xFF;
            final int argb = color = ColourUtils.toRGBA(red, green, blue, 255);
        }
        if (this.smooth.getValue()) {
            this.cFontRenderer.drawStringWithShadow("FPS: " + Minecraft.func_175610_ah(), this.x.getValue(), yCount - this.cFontRenderer.getHeight() - 1.0f, color);
            this.cFontRenderer.drawStringWithShadow("PING: " + ((PvPInfo.mc.func_147104_D() != null) ? Long.valueOf(PvPInfo.mc.func_147104_D().field_78844_e) : "0"), this.x.getValue(), (yCount += 10.0f) - this.cFontRenderer.getHeight() - 1.0f, color);
            this.cFontRenderer.drawStringWithShadow("TOTEMS: " + totems, this.x.getValue(), (yCount += 10.0f) - this.cFontRenderer.getHeight() - 1.0f, color);
            this.cFontRenderer.drawStringWithShadow("AT: " + this.getAutoTrap(), this.x.getValue(), (yCount += 10.0f) - this.cFontRenderer.getHeight() - 1.0f, color);
            this.cFontRenderer.drawStringWithShadow("SU: " + this.getSurround(), this.x.getValue(), (yCount += 10.0f) - this.cFontRenderer.getHeight() - 1.0f, color);
            this.cFontRenderer.drawStringWithShadow("CA: " + this.getCaura(), this.x.getValue(), (yCount += 10.0f) - this.cFontRenderer.getHeight() - 1.0f, color);
        }
        else {
            Wrapper.getMinecraft().field_71466_p.func_175063_a("FPS: " + Minecraft.func_175610_ah(), (float)this.x.getValue(), yCount - Wrapper.getMinecraft().field_71466_p.field_78288_b, color);
            Wrapper.getMinecraft().field_71466_p.func_175063_a("PING: " + ((PvPInfo.mc.func_147104_D() != null) ? Long.valueOf(PvPInfo.mc.func_147104_D().field_78844_e) : "0"), (float)this.x.getValue(), (yCount += 10.0f) - Wrapper.getMinecraft().field_71466_p.field_78288_b, color);
            Wrapper.getMinecraft().field_71466_p.func_175063_a("TOTEMS: " + totems, (float)this.x.getValue(), (yCount += 10.0f) - Wrapper.getMinecraft().field_71466_p.field_78288_b, color);
            Wrapper.getMinecraft().field_71466_p.func_175063_a("AT: " + this.getAutoTrap(), (float)this.x.getValue(), (yCount += 10.0f) - Wrapper.getMinecraft().field_71466_p.field_78288_b, color);
            Wrapper.getMinecraft().field_71466_p.func_175063_a("SU: " + this.getSurround(), (float)this.x.getValue(), (yCount += 10.0f) - Wrapper.getMinecraft().field_71466_p.field_78288_b, color);
            Wrapper.getMinecraft().field_71466_p.func_175063_a("CA: " + this.getCaura(), (float)this.x.getValue(), (yCount += 10.0f) - Wrapper.getMinecraft().field_71466_p.field_78288_b, color);
        }
    }
    
    private String getAutoTrap() {
        String x = "FALSE";
        if (ModuleManager.getModuleByName("AutoTrap") != null) {
            x = Boolean.toString(ModuleManager.getModuleByName("AutoTrap").isEnabled()).toUpperCase();
        }
        return x;
    }
    
    private String getSurround() {
        String x = "FALSE";
        if (ModuleManager.getModuleByName("Surround") != null) {
            x = Boolean.toString(ModuleManager.getModuleByName("Surround").isEnabled()).toUpperCase();
        }
        return x;
    }
    
    private String getCaura() {
        String x = "FALSE";
        if (ModuleManager.getModuleByName("CrystalAura") != null) {
            x = Boolean.toString(ModuleManager.getModuleByName("CrystalAura").isEnabled()).toUpperCase();
        }
        return x;
    }
}
