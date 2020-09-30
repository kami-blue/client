package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.kami.component.UnboundSlider;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.util.color.ColorHolder;
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter;

/**
 * Created by 086 on 17/12/2017.
 */
public class KamiUnboundSliderUI extends AbstractComponentUI<UnboundSlider> {

    @Override
    public void renderComponent(UnboundSlider component) {
        String s = component.getText() + ": " + component.getValue();

        ColorHolder color = new ColorHolder(
                component.isHovered() ? KamiGuiColors.GuiC.buttonHoveredN.color :
                        component.isPressed() ? KamiGuiColors.GuiC.buttonPressed.color :
                                KamiGuiColors.GuiC.buttonHoveredT.color);

        FontRenderAdapter.INSTANCE.drawString(s, component.getWidth() / 2f - FontRenderAdapter.INSTANCE.getStringWidth(s) / 2f, 1f, true, color);
    }

    @Override
    public void handleAddComponent(UnboundSlider component, Container container) {
        component.setHeight((int) (FontRenderAdapter.INSTANCE.getFontHeight() + 2));
        component.setWidth((int) FontRenderAdapter.INSTANCE.getStringWidth(component.getText()));
    }

}
