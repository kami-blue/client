package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.kami.component.ColorizedCheckButton;
import me.zeroeightsix.kami.gui.rgui.component.use.CheckButton;
import me.zeroeightsix.kami.util.color.ColorHolder;
import me.zeroeightsix.kami.util.graphics.GlStateUtils;
import me.zeroeightsix.kami.util.graphics.RenderUtils2D;
import me.zeroeightsix.kami.util.graphics.VertexHelper;
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter;
import me.zeroeightsix.kami.util.math.Vec2d;

import static me.zeroeightsix.kami.gui.kami.theme.kami.KamiGuiColors.GuiC;

/**
 * Created by 086 on 8/08/2017.
 */
public class RootColorizedCheckButtonUI extends RootCheckButtonUI<ColorizedCheckButton> {

    @Override
    public void renderComponent(CheckButton component) {
        ColorHolder color = new ColorHolder(GuiC.buttonIdleN.color);
        if (component.isHovered() || component.isPressed()) {
            color = new ColorHolder(GuiC.buttonPressed.color);
        }
        if (component.isToggled()) {
            color = new ColorHolder(GuiC.buttonIdleT.color);
        }

        VertexHelper vertexHelper = new VertexHelper(GlStateUtils.useVbo());
        RenderUtils2D.drawLine(vertexHelper, new Vec2d(0, component.getHeight()), new Vec2d(component.getWidth(), component.getHeight()), 2.5f, color);

        ColorHolder idleColour = new ColorHolder(component.isToggled() ? GuiC.buttonIdleT.color : GuiC.buttonIdleN.color);
        ColorHolder downColour = new ColorHolder(component.isToggled() ? GuiC.buttonHoveredT.color : GuiC.buttonHoveredN.color);

        FontRenderAdapter.INSTANCE.drawString(component.getName(), component.getWidth() / 2f - FontRenderAdapter.INSTANCE.getStringWidth(component.getName(), 0.75f) / 2f, 0, true, component.isPressed() ? downColour : idleColour, 0.75f);
    }
}
