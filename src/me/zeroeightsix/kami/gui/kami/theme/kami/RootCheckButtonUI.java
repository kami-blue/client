// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.kami.RenderHelper;
import me.zeroeightsix.kami.gui.kami.KamiGUI;
import org.lwjgl.opengl.GL11;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.setting.Setting;
import java.awt.Font;
import java.awt.Color;
import me.zeroeightsix.kami.gui.font.CFontRenderer;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.gui.rgui.component.use.CheckButton;

public class RootCheckButtonUI<T extends CheckButton> extends AbstractComponentUI<CheckButton>
{
    CFontRenderer cFontRenderer;
    protected Color backgroundColour;
    protected Color backgroundColourHover;
    protected Color idleColourNormal;
    protected Color downColourNormal;
    protected Color idleColourToggle;
    protected Color downColourToggle;
    public float redForBG;
    public float greenForBG;
    public float blueForBG;
    
    public RootCheckButtonUI() {
        this.cFontRenderer = new CFontRenderer(new Font("Verdana", 0, 18), true, false);
        this.backgroundColour = new Color(46, 88, 200);
        this.backgroundColourHover = new Color(81, 184, 255);
        this.idleColourNormal = new Color(200, 200, 200);
        this.downColourNormal = new Color(190, 190, 190);
        this.idleColourToggle = new Color(126, 206, 250);
        this.downColourToggle = this.idleColourToggle.brighter();
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
    public void renderComponent(final CheckButton component, final FontRenderer ff) {
        ModuleManager.getModuleByName("Gui").settingList.forEach(setting -> this.checkSettingGuiColour(setting));
        if (component.isToggled()) {
            GL11.glColor3f(1.0f, 1.0f, 1.0f);
            GL11.glColor3f(this.redForBG, this.greenForBG, this.blueForBG);
            RenderHelper.drawFilledRectangle(0.0f, (float)(KamiGUI.fontRenderer.getFontHeight() / 2 - 5), (float)component.getWidth(), KamiGUI.fontRenderer.getFontHeight() * 1.6f);
        }
        final String text = component.getName();
        int c = 16777215;
        if (component.isHovered()) {
            c = (c & 0x7F7F7F) << 1;
        }
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        if (ModuleManager.getModuleByName("SmoothFont").isDisabled()) {
            KamiGUI.fontRenderer.drawString(1, KamiGUI.fontRenderer.getFontHeight() / 2 - 2, c, text);
        }
        else {
            GL11.glEnable(3553);
            GL11.glEnable(3042);
            GL11.glDisable(2884);
            this.cFontRenderer.drawString(text, 1.0f, (float)(KamiGUI.fontRenderer.getFontHeight() / 2 - 2), c);
            GL11.glDisable(3553);
            GL11.glDisable(3042);
            GL11.glEnable(2884);
        }
    }
    
    @Override
    public void handleAddComponent(final CheckButton component, final Container container) {
        component.setWidth(KamiGUI.fontRenderer.getStringWidth("Dispenser32kne") + 2);
        component.setHeight(KamiGUI.fontRenderer.getFontHeight() + 2);
    }
}
