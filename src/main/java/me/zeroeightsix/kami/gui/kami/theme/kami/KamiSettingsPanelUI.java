package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.kami.RenderHelper;
import me.zeroeightsix.kami.gui.kami.component.SettingsPanel;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.module.modules.experimental.GUIColour;
import org.lwjgl.opengl.GL11;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

/**
 * Created by 086 on 16/12/2017.
 */
public class KamiSettingsPanelUI extends AbstractComponentUI<SettingsPanel> {

    @Override
    public void renderComponent(SettingsPanel component, FontRenderer fontRenderer) {
        super.renderComponent(component, fontRenderer);

        GL11.glLineWidth(2.0F);
        float red = (float) ((GUIColour) MODULE_MANAGER.getModule(GUIColour.class)).red.getValue() / 255.0F;
        float green = (float) ((GUIColour) MODULE_MANAGER.getModule(GUIColour.class)).green.getValue() / 255.0F;
        float blue = (float) ((GUIColour) MODULE_MANAGER.getModule(GUIColour.class)).blue.getValue() / 255.0F;
        float alpha = (float) ((GUIColour) MODULE_MANAGER.getModule(GUIColour.class)).alpha.getValue() / 255.0F;
        if (MODULE_MANAGER.getModule(GUIColour.class).isEnabled()) {
            GL11.glColor4f(red, green, blue, alpha);
        } else {
            GL11.glColor4f(0.17F, 0.17F, 0.18F, 0.9F);
        }

        RenderHelper.drawFilledRectangle(0.0F, 0.0F, (float) component.getWidth(), (float) component.getHeight());
        GL11.glColor3f(.60f, .56f, 1.00f);
        GL11.glLineWidth(1.5F);
        RenderHelper.drawRectangle(0.0F, 0.0F, (float) component.getWidth(), (float) component.getHeight());
    }
}
