// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import java.awt.Color;
import me.zeroeightsix.kami.module.ModuleManager;
import org.lwjgl.opengl.GL11;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import java.awt.Font;
import me.zeroeightsix.kami.gui.font.CFontRenderer;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Groupbox;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;

public class RootGroupboxUI extends AbstractComponentUI<Groupbox>
{
    CFontRenderer cFontRenderer;
    
    public RootGroupboxUI() {
        this.cFontRenderer = new CFontRenderer(new Font("Verdana", 0, 18), true, false);
    }
    
    @Override
    public void renderComponent(final Groupbox component, final FontRenderer fontRenderer) {
        GL11.glLineWidth(1.0f);
        if (ModuleManager.getModuleByName("SmoothFont").isEnabled()) {
            this.cFontRenderer.drawString(component.getName(), 1.0f, 1.0f, Color.white.getRGB());
        }
        else {
            fontRenderer.drawString(1, 1, component.getName());
        }
        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        GL11.glDisable(3553);
        GL11.glBegin(1);
        GL11.glVertex2d(0.0, 0.0);
        GL11.glVertex2d((double)component.getWidth(), 0.0);
        GL11.glVertex2d((double)component.getWidth(), 0.0);
        GL11.glVertex2d((double)component.getWidth(), (double)component.getHeight());
        GL11.glVertex2d((double)component.getWidth(), (double)component.getHeight());
        GL11.glVertex2d(0.0, (double)component.getHeight());
        GL11.glVertex2d(0.0, (double)component.getHeight());
        GL11.glVertex2d(0.0, 0.0);
        GL11.glEnd();
    }
    
    @Override
    public void handleMouseDown(final Groupbox component, final int x, final int y, final int button) {
    }
    
    @Override
    public void handleAddComponent(final Groupbox component, final Container container) {
        component.setWidth(100);
        component.setHeight(200);
        component.setOriginOffsetY(component.getTheme().getFontRenderer().getFontHeight() + 3);
    }
}
