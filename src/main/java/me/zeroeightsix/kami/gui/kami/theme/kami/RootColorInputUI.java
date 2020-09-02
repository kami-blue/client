package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.kami.RenderHelper;
import me.zeroeightsix.kami.gui.kami.RootSmallFontRenderer;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.component.use.ColorInput;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;

import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glLineWidth;

/**
 * Created by Guac on 18/08/2020.
 */
public class RootColorInputUI extends AbstractComponentUI<ColorInput> {

    RootSmallFontRenderer smallFontRenderer = new RootSmallFontRenderer();

    @Override
    public void renderComponent(ColorInput component, FontRenderer aa) {
        int bSize = component.getSize();
        glColor3f(.60f, .56f, 1.00f);
        glLineWidth(1f);
        RenderHelper.drawRectangle(0, 0, component.getWidth() - bSize - 4, component.getTheme().getFontRenderer().getFontHeight());
        component.getValue().setGLColour();
        RenderHelper.drawFilledRectangle(component.getWidth() - bSize, 0, bSize, bSize);
    }

    @Override
    public void handleAddComponent(ColorInput component, Container container) {
        component.setWidth(smallFontRenderer.getStringWidth(component.getText()) + smallFontRenderer.getStringWidth(component.getMaximumWidth() + "") + 3);
        component.setHeight(component.getTheme().getFontRenderer().getFontHeight() + 2);
    }
}