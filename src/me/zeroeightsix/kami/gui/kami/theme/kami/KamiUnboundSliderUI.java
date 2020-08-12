// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import org.lwjgl.opengl.GL11;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import java.awt.Font;
import me.zeroeightsix.kami.gui.font.CFontRenderer;
import me.zeroeightsix.kami.gui.kami.component.UnboundSlider;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;

public class KamiUnboundSliderUI extends AbstractComponentUI<UnboundSlider>
{
    CFontRenderer cFontRenderer;
    
    public KamiUnboundSliderUI() {
        this.cFontRenderer = new CFontRenderer(new Font("Verdana", 0, 18), true, false);
    }
    
    @Override
    public void renderComponent(final UnboundSlider component, final FontRenderer fontRenderer) {
        final String s = component.getText() + ": " + component.getValue();
        int c = component.isPressed() ? 11184810 : 14540253;
        if (component.isHovered()) {
            c = (c & 0x7F7F7F) << 1;
        }
        if (ModuleManager.getModuleByName("SmoothFont").isEnabled()) {
            GL11.glDisable(2884);
            GL11.glEnable(3042);
            GL11.glEnable(3553);
            this.cFontRenderer.drawString(s, (float)(component.getWidth() / 2 - fontRenderer.getStringWidth(s) / 2), (float)(component.getHeight() - fontRenderer.getFontHeight() / 2 - 4), c);
            GL11.glEnable(2884);
            GL11.glDisable(3042);
            GL11.glDisable(3553);
        }
        else {
            fontRenderer.drawString(component.getWidth() / 2 - fontRenderer.getStringWidth(s) / 2, component.getHeight() - fontRenderer.getFontHeight() / 2 - 4, c, s);
        }
        GL11.glDisable(3042);
    }
    
    @Override
    public void handleAddComponent(final UnboundSlider component, final Container container) {
        component.setHeight(component.getTheme().getFontRenderer().getFontHeight());
        component.setWidth(component.getTheme().getFontRenderer().getStringWidth(component.getText()));
    }
}
