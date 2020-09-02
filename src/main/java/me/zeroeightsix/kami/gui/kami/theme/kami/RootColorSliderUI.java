package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.kami.RenderHelper;
import me.zeroeightsix.kami.gui.kami.RootSmallFontRenderer;
import me.zeroeightsix.kami.gui.kami.theme.kami.KamiGuiColors.GuiC;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.component.use.ColorSlider;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.util.HSBColourHolder;

import static me.zeroeightsix.kami.util.ColourConverter.toF;
import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Guac on 10/8/2020.
 */
public class RootColorSliderUI extends AbstractComponentUI<ColorSlider> {

    RootSmallFontRenderer smallFontRenderer = new RootSmallFontRenderer();

    @Override
    public void renderComponent(ColorSlider component, FontRenderer aa) {
        glColor4f(toF(GuiC.sliderColour.color.getRed()), toF(GuiC.sliderColour.color.getGreen()), toF(GuiC.sliderColour.color.getBlue()), component.getOpacity());
        glLineWidth(10f);
        int width = component.getWidth();
        int height = component.getHeight();
        HSBColourHolder value = component.getValue();
        double w = width * (value.getH());
        float downscale = 2f;
        glShadeModel(GL_SMOOTH);
        glBegin(GL_LINE_STRIP);
            glColor3f(1f, 0f, 0f); //Red
            glVertex2d(0, height / downscale);
            glColor3f(1f, 1f, 0f); //Yellow
            glVertex2d(width / 6, height / downscale);
            glColor3f(0f, 1f, 0f); //Green
            glVertex2d(width / 3, height / downscale);
            glColor3f(0f, 1f, 1f); //Light blue
            glVertex2d(width / 2, height / downscale);
            glColor3f(0f, 0f, 1f); //Blue
            glVertex2d(width / 1.5, height / downscale);
            glColor3f(1f, 0f, 1f); //Purple
            glVertex2d(width / 1.2, height / downscale);
            glColor3f(1f, 0f, 0f); //Red
            glVertex2d(width, height / downscale);
        glEnd();

        glColor3f(1f, 1f, 1f);
        RenderHelper.drawTriangle( w, height / downscale - 3, 5, 5, 0);
        glColor3f(toF(GuiC.sliderColour.color.getRed()), toF(GuiC.sliderColour.color.getGreen()), toF(GuiC.sliderColour.color.getBlue()));
    }

    @Override
    public void handleAddComponent(ColorSlider component, Container container) {
        component.setWidth(smallFontRenderer.getStringWidth(component.getText()) + smallFontRenderer.getStringWidth(component.getMaximum() + "") + 3);
        component.setHeight(2);
    }

}
