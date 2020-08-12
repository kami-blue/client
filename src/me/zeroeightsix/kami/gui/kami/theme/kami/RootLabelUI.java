// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.rgui.component.Component;
import java.awt.Color;
import org.lwjgl.opengl.GL11;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.gui.rgui.component.AlignedComponent;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import java.awt.Font;
import me.zeroeightsix.kami.gui.font.CFontRenderer;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.gui.rgui.component.use.Label;

public class RootLabelUI<T extends Label> extends AbstractComponentUI<Label>
{
    CFontRenderer cFontRenderer;
    
    public RootLabelUI() {
        this.cFontRenderer = new CFontRenderer(new Font("Verdana", 0, 18), true, false);
    }
    
    @Override
    public void renderComponent(final Label component, FontRenderer a) {
        a = component.getFontRenderer();
        final String[] lines = component.getLines();
        int y = 0;
        final boolean shadow = component.isShadow();
        for (final String s : lines) {
            int x = 0;
            if (component.getAlignment() == AlignedComponent.Alignment.CENTER) {
                x = component.getWidth() / 2 - a.getStringWidth(s) / 2;
            }
            else if (component.getAlignment() == AlignedComponent.Alignment.RIGHT) {
                x = component.getWidth() - a.getStringWidth(s);
            }
            if (shadow) {
                if (ModuleManager.getModuleByName("SmoothFont").isEnabled()) {
                    GL11.glDisable(2884);
                    GL11.glEnable(3042);
                    GL11.glEnable(3553);
                    this.cFontRenderer.drawStringWithShadow(s, x, y, Color.white.getRGB());
                    GL11.glEnable(2884);
                    GL11.glDisable(3042);
                    GL11.glDisable(3553);
                }
                else {
                    a.drawStringWithShadow(x, y, 255, 255, 255, s);
                }
            }
            else if (ModuleManager.getModuleByName("SmoothFont").isEnabled()) {
                GL11.glDisable(2884);
                GL11.glEnable(3042);
                GL11.glEnable(3553);
                this.cFontRenderer.drawString(s, (float)x, (float)y, Color.white.getRGB());
                GL11.glEnable(2884);
                GL11.glDisable(3042);
                GL11.glDisable(3553);
            }
            else {
                a.drawString(x, y, s);
            }
            y += a.getFontHeight() + 3;
        }
        GL11.glDisable(3553);
        GL11.glDisable(3042);
    }
    
    @Override
    public void handleSizeComponent(final Label component) {
        final String[] lines = component.getLines();
        int y = 0;
        int w = 0;
        for (final String s : lines) {
            w = Math.max(w, component.getFontRenderer().getStringWidth(s));
            y += component.getFontRenderer().getFontHeight() + 3;
        }
        component.setWidth(w);
        component.setHeight(y);
    }
}
