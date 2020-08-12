// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.kami.RenderHelper;
import org.lwjgl.opengl.GL11;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.setting.Setting;
import java.awt.Font;
import me.zeroeightsix.kami.gui.font.CFontRenderer;
import me.zeroeightsix.kami.gui.kami.RootSmallFontRenderer;
import me.zeroeightsix.kami.gui.rgui.component.use.Slider;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;

public class RootSliderUI extends AbstractComponentUI<Slider>
{
    RootSmallFontRenderer smallFontRenderer;
    CFontRenderer cFontRenderer;
    public float redForBG;
    public float greenForBG;
    public float blueForBG;
    
    public RootSliderUI() {
        this.smallFontRenderer = new RootSmallFontRenderer();
        this.cFontRenderer = new CFontRenderer(new Font("Verdana", 0, 18), true, false);
    }
    
    private void checkSettingGuiColour(final Setting setting) {
        final String name3;
        final String s;
        final String name2 = s = (name3 = setting.getName());
        switch (s) {
            case "Border Red": {
                this.redForBG = setting.getValue();
                break;
            }
            case "Border Green": {
                this.greenForBG = setting.getValue();
                break;
            }
            case "Border Blue": {
                this.blueForBG = setting.getValue();
                break;
            }
        }
    }
    
    @Override
    public void renderComponent(final Slider component, final FontRenderer aa) {
        ModuleManager.getModuleByName("Gui").settingList.forEach(setting -> this.checkSettingGuiColour(setting));
        GL11.glColor4f(this.redForBG, this.greenForBG, this.blueForBG, component.getOpacity());
        GL11.glLineWidth(2.5f);
        final int height = component.getHeight();
        double w = component.getWidth() * component.getValue() / component.getMaximum();
        final float downscale = 1.1f;
        GL11.glBegin(1);
        GL11.glVertex2d(0.0, (double)(height / downscale));
        GL11.glVertex2d(w, (double)(height / downscale));
        GL11.glColor3f(0.33f, 0.33f, 0.33f);
        GL11.glVertex2d(w, (double)(height / downscale));
        GL11.glVertex2d((double)component.getWidth(), (double)(height / downscale));
        GL11.glEnd();
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        RenderHelper.drawCircle((float)(int)w, height / downscale, 2.0f);
        String s = "";
        if (Math.floor(component.getValue()) == component.getValue()) {
            s += (int)component.getValue();
        }
        else {
            s += component.getValue();
        }
        if (component.isPressed()) {
            w -= this.smallFontRenderer.getStringWidth(s) / 2;
            w = Math.max(0.0, Math.min(w, component.getWidth() - this.smallFontRenderer.getStringWidth(s)));
            this.smallFontRenderer.drawString((int)w, 0, s);
        }
        else {
            this.smallFontRenderer.drawString(0, 0, component.getText());
            this.smallFontRenderer.drawString(component.getWidth() - this.smallFontRenderer.getStringWidth(s), 0, s);
        }
        GL11.glDisable(3553);
    }
    
    @Override
    public void handleAddComponent(final Slider component, final Container container) {
        component.setHeight(component.getTheme().getFontRenderer().getFontHeight() + 2);
        component.setWidth(this.smallFontRenderer.getStringWidth(component.getText()) + this.smallFontRenderer.getStringWidth(component.getMaximum() + "") + 3);
    }
}
