package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen;
import me.zeroeightsix.kami.gui.kami.component.SettingsPanel;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.component.use.CheckButton;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.gui.rgui.util.ContainerHelper;
import me.zeroeightsix.kami.module.modules.client.Tooltips;
import me.zeroeightsix.kami.util.Wrapper;
import me.zeroeightsix.kami.util.color.ColorHolder;
import me.zeroeightsix.kami.util.graphics.GlStateUtils;
import me.zeroeightsix.kami.util.graphics.RenderUtils2D;
import me.zeroeightsix.kami.util.graphics.VertexHelper;
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter;
import me.zeroeightsix.kami.util.math.Vec2d;
import org.lwjgl.input.Mouse;

import java.util.List;

import static me.zeroeightsix.kami.gui.kami.theme.kami.KamiGuiColors.GuiC;
import static org.lwjgl.opengl.GL11.*;

/**
 * Created by 086 on 4/08/2017.
 * Tooltips added by l1ving on 13/04/20
 */
public class RootCheckButtonUI<T extends CheckButton> extends AbstractComponentUI<CheckButton> {
    @Override
    public void renderComponent(CheckButton component) {
        String text = component.getName(); // on toggle, toggled, hovered enabled
        ColorHolder color = new ColorHolder(
                component.isHovered() ? GuiC.buttonHoveredN.color :
                        component.isPressed() ? GuiC.buttonPressed.color :
                                component.isToggled() ? GuiC.buttonIdleT.color :
                                        GuiC.buttonHoveredT.color);
        if (component.isHovered()) {
            if (component.hasDescription() && !isSettingsOpen() && Tooltips.INSTANCE.isEnabled()) {
                Component componentAt = KamiMod.INSTANCE.getGuiManager().getComponentAt(DisplayGuiScreen.mouseX, DisplayGuiScreen.mouseY);
                if (componentAt.getHeight() != 11)
                    return; // PREVENT DRAWING WHEN OUTSIDE THE CONTAINER // 11 is height of the regular module

                if (componentAt.getWidth() != component.getWidth())
                    return; // prevent drawing 2 different categories when overlapped

                glDisable(GL_SCISSOR_TEST); // let it draw outside of the container
                glTranslatef(0, 0, 69);

                float tooltipX = 14; // padding
                float tooltipWidth = FontRenderAdapter.INSTANCE.getStringWidth(component.getDescription()) + 8f;

                boolean tooBig = Wrapper.getMinecraft().displayWidth < (Mouse.getX() + (tooltipWidth * 2f + (component.getWidth() * 2)));

                if (tooBig) {
                    tooltipX = -tooltipX - tooltipWidth - component.getWidth();
                }

                VertexHelper vertexHelper = new VertexHelper(GlStateUtils.useVbo());
                Vec2d pos1 = new Vec2d(component.getWidth() + tooltipX, -2);
                Vec2d pos2 = pos1.add(tooltipWidth, FontRenderAdapter.INSTANCE.getFontHeight() + 2);

                RenderUtils2D.drawRectFilled(vertexHelper, pos1, pos2, new ColorHolder(GuiC.windowFilled.color));
                RenderUtils2D.drawRectOutline(vertexHelper, pos1, pos2, 1.5f, new ColorHolder(GuiC.windowOutline.color));
                FontRenderAdapter.INSTANCE.drawString(component.getDescription(), component.getWidth() + tooltipX + 4f, -1.0f, false);

                glTranslatef(0, 0, -69);
                glEnable(GL_SCISSOR_TEST); // stop drawing outside of the container
            }
        }

        FontRenderAdapter.INSTANCE.drawString(text, component.getWidth() / 2f - FontRenderAdapter.INSTANCE.getStringWidth(text) / 2f, 1f, false, color);
    }

    @Override
    public void handleAddComponent(CheckButton component, Container container) {
        component.setWidth((int) (FontRenderAdapter.INSTANCE.getStringWidth(component.getName()) + 14));
        component.setHeight((int) (FontRenderAdapter.INSTANCE.getFontHeight() + 2));
    }

    /* in all honesty this is probably resource inefficient but there isn't any other way of getting panels :/ */
    private boolean isSettingsOpen() {
        List<SettingsPanel> panels = ContainerHelper.getAllChildren(SettingsPanel.class, KamiMod.INSTANCE.getGuiManager());
        for (SettingsPanel settingsPanel : panels) {
            if (settingsPanel.isVisible()) {
                return true;
            }
        }
        return false;
    }
}
