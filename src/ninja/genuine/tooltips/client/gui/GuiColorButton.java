// 
// Decompiled by Procyon v0.5.36
// 

package ninja.genuine.tooltips.client.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Property;
import net.minecraft.client.gui.GuiButton;

public class GuiColorButton extends GuiButton
{
    public String value;
    public String defaultV;
    
    public GuiColorButton(final int buttonId, final int x, final int y, final Property property) {
        this(buttonId, x, y, property.getName(), property.getDefault());
    }
    
    public GuiColorButton(final int buttonId, final int x, final int y, final String value, final String defaultV) {
        super(buttonId, x, y, 20, 20, value);
        this.setValues(value, defaultV);
    }
    
    public void setValues(final String value, final String defaultV) {
        this.value = value;
        this.defaultV = defaultV;
    }
    
    public void update(final String value) {
        this.value = value;
    }
    
    public void func_191745_a(final Minecraft mc, final int mouseX, final int mouseY, final float partialTick) {
        if (!this.field_146125_m) {
            return;
        }
        int color = 0;
        try {
            color = Integer.decode(this.value);
        }
        catch (NumberFormatException e) {
            color = Integer.decode(this.defaultV);
        }
        this.field_146123_n = (mouseX >= this.field_146128_h && mouseY >= this.field_146129_i && mouseX < this.field_146128_h + this.field_146120_f && mouseY < this.field_146129_i + this.field_146121_g);
        final int i = this.func_146114_a(this.field_146123_n);
        mc.func_110434_K().func_110577_a(GuiColorButton.field_146122_a);
        GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.func_179147_l();
        GlStateManager.func_187428_a(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.func_187401_a(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        this.func_73729_b(this.field_146128_h, this.field_146129_i, 0, 46 + i * 20, this.field_146120_f / 2, this.field_146121_g);
        this.func_73729_b(this.field_146128_h + this.field_146120_f / 2, this.field_146129_i, 200 - this.field_146120_f / 2, 46 + i * 20, this.field_146120_f / 2, this.field_146121_g);
        this.func_73733_a(this.field_146128_h + 2, this.field_146129_i + 2, this.field_146128_h + 20 - 2, this.field_146129_i + 20 - 2, color | 0xFF000000, color | 0xFF000000);
        this.func_146119_b(mc, mouseX, mouseY);
    }
}
