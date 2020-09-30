package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.component.use.Button;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.util.color.ColorHolder;
import me.zeroeightsix.kami.util.graphics.GlStateUtils;
import me.zeroeightsix.kami.util.graphics.RenderUtils2D;
import me.zeroeightsix.kami.util.graphics.VertexHelper;
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter;
import me.zeroeightsix.kami.util.math.Vec2d;

/**
 * Created by 086 on 25/06/2017.
 */
public class RootButtonUI<T extends Button> extends AbstractComponentUI<Button> {

    protected ColorHolder idleColour = new ColorHolder(163, 163, 163);
    protected ColorHolder downColour = new ColorHolder(255, 255, 255);

    @Override
    public void renderComponent(Button component) {
        ColorHolder color = new ColorHolder(
                component.isHovered() ? KamiGuiColors.GuiC.buttonHoveredN.color :
                        component.isPressed() ? KamiGuiColors.GuiC.buttonPressed.color :
                                KamiGuiColors.GuiC.buttonHoveredT.color);

        VertexHelper vertexHelper = new VertexHelper(GlStateUtils.useVbo());

        RenderUtils2D.drawRoundedRectFilled(vertexHelper, new Vec2d(0, 0), new Vec2d(component.getWidth(), component.getHeight()), 3, 8, color);

        FontRenderAdapter.INSTANCE.drawString(component.getName(), component.getWidth() / 2f - FontRenderAdapter.INSTANCE.getStringWidth(component.getName()) / 2f, 0, true, component.isPressed() ? downColour : idleColour);
    }

    @Override
    public void handleAddComponent(Button component, Container container) {
        component.setWidth((int) (FontRenderAdapter.INSTANCE.getStringWidth(component.getName()) + 28));
        component.setHeight((int) (FontRenderAdapter.INSTANCE.getFontHeight() + 2));
    }
}
