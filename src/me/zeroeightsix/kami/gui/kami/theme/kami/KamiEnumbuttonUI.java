// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.rgui.poof.IPoof;
import me.zeroeightsix.kami.gui.rgui.poof.PoofInfo;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.kami.component.EnumButton.EnumbuttonIndexPoof;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.module.ModuleManager;
import org.lwjgl.opengl.GL11;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import java.awt.Font;
import java.awt.Color;
import me.zeroeightsix.kami.gui.font.CFontRenderer;
import me.zeroeightsix.kami.gui.kami.RootSmallFontRenderer;
import me.zeroeightsix.kami.gui.kami.component.EnumButton;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;

public class KamiEnumbuttonUI extends AbstractComponentUI<EnumButton>
{
    RootSmallFontRenderer smallFontRenderer;
    CFontRenderer cfontRenderer;
    protected Color idleColour;
    protected Color downColour;
    EnumButton modeComponent;
    long lastMS;
    
    public KamiEnumbuttonUI() {
        this.smallFontRenderer = new RootSmallFontRenderer();
        this.cfontRenderer = new CFontRenderer(new Font("Verdana", 0, 18), true, false);
        this.idleColour = new Color(163, 163, 163);
        this.downColour = new Color(0, 0, 0);
        this.lastMS = System.currentTimeMillis();
    }
    
    @Override
    public void renderComponent(final EnumButton component, final FontRenderer aa) {
        if (System.currentTimeMillis() - this.lastMS > 3000L && this.modeComponent != null) {
            this.modeComponent = null;
        }
        int c = component.isPressed() ? 11184810 : 14540253;
        if (component.isHovered()) {
            c = (c & 0x7F7F7F) << 1;
        }
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        GL11.glEnable(3553);
        final int parts = component.getModes().length;
        final double step = component.getWidth() / parts;
        final double startX = step * component.getIndex();
        final double endX = step * (component.getIndex() + 1);
        final int height = component.getHeight();
        final float downscale = 1.1f;
        GL11.glDisable(3553);
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        GL11.glBegin(1);
        GL11.glVertex2d(startX, (double)(height / downscale));
        GL11.glVertex2d(endX, (double)(height / downscale));
        GL11.glEnd();
        if (this.modeComponent == null || !this.modeComponent.equals(component)) {
            if (ModuleManager.getModuleByName("SmoothFont").isEnabled()) {
                GL11.glDisable(2884);
                GL11.glEnable(3042);
                GL11.glEnable(3553);
                this.cfontRenderer.drawStringWithShadow(component.getName(), 0.0, 0.0, c);
                this.cfontRenderer.drawStringWithShadow(component.getIndexMode(), component.getWidth() - this.smallFontRenderer.getStringWidth(component.getIndexMode()), 0.0, c);
                GL11.glEnable(2884);
                GL11.glDisable(3042);
                GL11.glDisable(3553);
            }
            else {
                this.smallFontRenderer.drawString(0, 0, c, component.getName());
                this.smallFontRenderer.drawString(component.getWidth() - this.smallFontRenderer.getStringWidth(component.getIndexMode()), 0, c, component.getIndexMode());
            }
        }
        else if (ModuleManager.getModuleByName("SmoothFont").isEnabled()) {
            GL11.glDisable(2884);
            GL11.glEnable(3042);
            GL11.glEnable(3553);
            this.cfontRenderer.drawStringWithShadow(component.getIndexMode(), component.getWidth() - this.smallFontRenderer.getStringWidth(component.getIndexMode()), 0.0, c);
            GL11.glEnable(2884);
            GL11.glDisable(3042);
            GL11.glDisable(3553);
        }
        else {
            this.smallFontRenderer.drawString(component.getWidth() / 2 - this.smallFontRenderer.getStringWidth(component.getIndexMode()) / 2, 0, c, component.getIndexMode());
        }
        GL11.glDisable(3042);
    }
    
    @Override
    public void handleSizeComponent(final EnumButton component) {
        int width = 0;
        for (final String s : component.getModes()) {
            width = Math.max(width, this.smallFontRenderer.getStringWidth(s));
        }
        component.setWidth(this.smallFontRenderer.getStringWidth(component.getName()) + width + 1);
        component.setHeight(this.smallFontRenderer.getFontHeight() + 2);
    }
    
    @Override
    public void handleAddComponent(final EnumButton component, final Container container) {
        component.addPoof(new EnumButton.EnumbuttonIndexPoof<EnumButton, EnumButton.EnumbuttonIndexPoof.EnumbuttonInfo>() {
            @Override
            public void execute(final EnumButton component, final EnumbuttonInfo info) {
                KamiEnumbuttonUI.this.modeComponent = component;
                KamiEnumbuttonUI.this.lastMS = System.currentTimeMillis();
            }
        });
    }
}
