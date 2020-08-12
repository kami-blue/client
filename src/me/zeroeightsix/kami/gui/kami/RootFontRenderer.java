// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.kami;

import org.lwjgl.opengl.GL11;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;

public class RootFontRenderer implements FontRenderer
{
    private final float fontsize;
    private final net.minecraft.client.gui.FontRenderer fontRenderer;
    
    public RootFontRenderer(final float fontsize) {
        this.fontRenderer = Minecraft.func_71410_x().field_71466_p;
        this.fontsize = fontsize;
    }
    
    @Override
    public int getFontHeight() {
        return (int)(Minecraft.func_71410_x().field_71466_p.field_78288_b * this.fontsize);
    }
    
    @Override
    public int getStringHeight(final String text) {
        return this.getFontHeight();
    }
    
    @Override
    public int getStringWidth(final String text) {
        return (int)(this.fontRenderer.func_78256_a(text) * this.fontsize);
    }
    
    @Override
    public void drawString(final int x, final int y, final String text) {
        this.drawString(x, y, 255, 255, 255, text);
    }
    
    @Override
    public void drawString(final int x, final int y, final int r, final int g, final int b, final String text) {
        this.drawString(x, y, 0xFF000000 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF), text);
    }
    
    @Override
    public void drawString(final int x, final int y, final Color color, final String text) {
        this.drawString(x, y, color.getRGB(), text);
    }
    
    @Override
    public void drawString(final int x, final int y, final int colour, final String text) {
        this.drawString(x, y, colour, text, true);
    }
    
    public void drawString(final int x, final int y, final int colour, final String text, final boolean shadow) {
        this.prepare(x, y);
        Minecraft.func_71410_x().field_71466_p.func_175065_a(text, 0.0f, 0.0f, colour, shadow);
        this.pop(x, y);
    }
    
    @Override
    public void drawStringWithShadow(final int x, final int y, final int r, final int g, final int b, final String text) {
        this.drawString(x, y, 0xFF000000 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF), text, true);
    }
    
    private void prepare(final int x, final int y) {
        GL11.glEnable(3553);
        GL11.glEnable(3042);
        GL11.glTranslatef((float)x, (float)y, 0.0f);
        GL11.glScalef(this.fontsize, this.fontsize, 1.0f);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    private void pop(final int x, final int y) {
        GL11.glScalef(1.0f / this.fontsize, 1.0f / this.fontsize, 1.0f);
        GL11.glTranslatef((float)(-x), (float)(-y), 0.0f);
        GL11.glDisable(3553);
    }
}
