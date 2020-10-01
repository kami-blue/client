package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.kami.theme.kami.KamiGuiColors.GuiC;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.component.use.Slider;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.util.color.ColorHolder;
import me.zeroeightsix.kami.util.graphics.GlStateUtils;
import me.zeroeightsix.kami.util.graphics.RenderUtils2D;
import me.zeroeightsix.kami.util.graphics.VertexHelper;
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter;
import me.zeroeightsix.kami.util.math.Vec2d;

/**
 * Created by 086 on 8/08/2017.
 * updated by Xiaro on 04/08/20
 */
public class RootSliderUI extends AbstractComponentUI<Slider> {

    @Override
    public void renderComponent(Slider component) {
        int height = component.getHeight();
        double value = component.getValue();
        double width = component.getWidth() * ((value - component.getMinimum()) / (component.getMaximum() - component.getMinimum()));

        VertexHelper vertexHelper = new VertexHelper(GlStateUtils.useVbo());

        ColorHolder sliderColor = new ColorHolder(GuiC.sliderColour.color);
        sliderColor.setA((int) (component.getOpacity() * 255f));

        RenderUtils2D.drawLine(vertexHelper, new Vec2d(0, height - 1), new Vec2d(width, height - 1), 2.5f, sliderColor);
        RenderUtils2D.drawLine(vertexHelper, new Vec2d(width, height - 1), new Vec2d(component.getWidth(), height - 1), 2.5f, new ColorHolder(84, 84, 84));
        RenderUtils2D.drawCircleFilled(vertexHelper, new Vec2d(width, height - 1), 2.0, 16, new ColorHolder(GuiC.sliderColour.color));

        String s = value + "";
        if (component.isPressed()) {
            width -= FontRenderAdapter.INSTANCE.getStringWidth(s, 0.75f) / 2f;
            width = Math.max(0, Math.min(width, component.getWidth() - FontRenderAdapter.INSTANCE.getStringWidth(s, 0.75f)));
            FontRenderAdapter.INSTANCE.drawString(s, (float) width, 0, true, new ColorHolder(255, 255, 255), 0.75f);
        } else {
            FontRenderAdapter.INSTANCE.drawString(component.getText(), 0, 0, true, new ColorHolder(255, 255, 255), 0.75f);
            FontRenderAdapter.INSTANCE.drawString(s, component.getWidth() - FontRenderAdapter.INSTANCE.getStringWidth(s, 0.75f), 0, true, new ColorHolder(255, 255, 255), 0.75f);
        }
    }

    @Override
    public void handleAddComponent(Slider component, Container container) {
        component.setHeight((int) (FontRenderAdapter.INSTANCE.getFontHeight() + 2));
        component.setWidth((int) (FontRenderAdapter.INSTANCE.getStringWidth(component.getText(), 0.75f) + FontRenderAdapter.INSTANCE.getStringWidth(component.getMaximum() + "", 0.75f) + 3));
    }
}
