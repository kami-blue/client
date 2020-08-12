// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.kami.RenderHelper;
import org.lwjgl.opengl.GL11;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.gui.kami.component.SettingsPanel;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;

public class KamiSettingsPanelUI extends AbstractComponentUI<SettingsPanel>
{
    public float redForBG;
    public float greenForBG;
    public float blueForBG;
    
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
    public void renderComponent(final SettingsPanel component, final FontRenderer fontRenderer) {
        super.renderComponent(component, fontRenderer);
        ModuleManager.getModuleByName("Gui").settingList.forEach(setting -> this.checkSettingGuiColour(setting));
        GL11.glLineWidth(2.0f);
        GL11.glColor4f(0.17f, 0.17f, 0.18f, 0.9f);
        RenderHelper.drawFilledRectangle(0.0f, 0.0f, (float)component.getWidth(), (float)component.getHeight());
        GL11.glColor3f(this.redForBG, this.greenForBG, this.blueForBG);
        GL11.glLineWidth(1.5f);
        RenderHelper.drawRectangle(0.0f, 0.0f, (float)component.getWidth(), (float)component.getHeight());
    }
}
