package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.kami.component.EnumButton;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
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
public class KamiEnumButtonUI extends AbstractComponentUI<EnumButton> {

    EnumButton modeComponent;
    long lastMS = System.currentTimeMillis();

    @Override
    public void renderComponent(EnumButton component) {
        if (System.currentTimeMillis() - lastMS > 3000 && modeComponent != null) {
            modeComponent = null;
        }

        ColorHolder color = new ColorHolder(
                component.isHovered() ? GuiC.buttonHoveredN.color :
                        component.isPressed() ? GuiC.buttonPressed.color :
                                GuiC.buttonHoveredT.color);

        int parts = component.getModes().length;
        double step = component.getWidth() / (double) parts;
        double startX = step * component.getIndex();
        double endX = step * (component.getIndex() + 1);

        int height = component.getHeight();

        VertexHelper vertexHelper = new VertexHelper(GlStateUtils.useVbo());
        RenderUtils2D.drawLine(vertexHelper, new Vec2d(startX, height - 1), new Vec2d(endX, height - 1), 1.5f, new ColorHolder(GuiC.sliderColour.color));

        if (modeComponent == null || !modeComponent.equals(component)) {
            FontRenderAdapter.INSTANCE.drawString(component.getName(), 0, 1f, true, color, 0.75f);
            FontRenderAdapter.INSTANCE.drawString(component.getIndexMode(), component.getWidth() - FontRenderAdapter.INSTANCE.getStringWidth(component.getIndexMode(), 0.75f), 1f, true, color, 0.75f);
        } else {
            FontRenderAdapter.INSTANCE.drawString(component.getIndexMode(), component.getWidth() / 2f - FontRenderAdapter.INSTANCE.getStringWidth(component.getIndexMode(), 0.75f) / 2f, 1f, true, color, 0.75f);
        }
    }

    @Override
    public void handleSizeComponent(EnumButton component) {
        int width = 0;
        for (String s : component.getModes()) {
            width = Math.max(width, (int) FontRenderAdapter.INSTANCE.getStringWidth(s, 0.75f));
        }
        component.setWidth((int) (FontRenderAdapter.INSTANCE.getStringWidth(component.getName(), 0.75f) + width + 1));
        component.setHeight((int) (FontRenderAdapter.INSTANCE.getFontHeight() + 2));
    }

    @Override
    public void handleAddComponent(EnumButton component, Container container) {
        component.addPoof(new EnumButton.EnumbuttonIndexPoof<EnumButton, EnumButton.EnumbuttonIndexPoof.EnumbuttonInfo>() {
            @Override
            public void execute(EnumButton component, EnumbuttonInfo info) {
                modeComponent = component;
                lastMS = System.currentTimeMillis();
            }
        });
    }
}
