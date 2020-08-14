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
        int bSize = 5;
        int width = component.getWidth();
        int height = component.getHeight();
        HSBColourHolder value = component.getValue();
        double w = width * (value.getH());
        float downscale = 1.1f;
        glShadeModel(GL_SMOOTH);
        glBegin(GL_LINE_STRIP);
            glColor3f(255, 0, 0); //Red
            glVertex2d(0, height / downscale);
            glColor3f(255, 255, 0); //Yellow
            glVertex2d(width / 6, height / downscale);
            glColor3f(0, 255, 0); //Green
            glVertex2d(width / 3, height / downscale);
            glColor3f(0, 255, 255); //Light blue
            glVertex2d(width / 2, height / downscale);
            glColor3f(0, 0, 255); //Blue
            glVertex2d(width / 1.5, height / downscale);
            glColor3f(255, 0, 255); //Purple
            glVertex2d(width / 1.2, height / downscale);
            glColor3f(255, 0, 0); //Red
            glVertex2d(width, height / downscale);
        glEnd();

        glColor3f(toF(GuiC.sliderColour.color.getRed()), toF(GuiC.sliderColour.color.getGreen()), toF(GuiC.sliderColour.color.getBlue()));
        RenderHelper.drawCircle((int) w, height / downscale, 2f);

        String s = "C";
        if (component.isPressed()) {
            w -= bSize / 2f;
            w = Math.max(0, Math.min(w, component.getWidth() - smallFontRenderer.getStringWidth(s)));
            glColor3f(toF(value.getRed()), toF(value.getGreen()), toF(value.getBlue()));
            RenderHelper.drawFilledRectangle((int)w, 0, bSize, bSize);
            glColor3f(toF(GuiC.sliderColour.color.getRed()), toF(GuiC.sliderColour.color.getGreen()), toF(GuiC.sliderColour.color.getBlue()));
        } else {
            smallFontRenderer.drawString(0, 0, component.getText());
            glColor3f(toF(value.getRed()), toF(value.getGreen()), toF(value.getBlue()));
            RenderHelper.drawFilledRectangle(component.getWidth() - bSize, 0, bSize, bSize);
            glColor3f(toF(GuiC.sliderColour.color.getRed()), toF(GuiC.sliderColour.color.getGreen()), toF(GuiC.sliderColour.color.getBlue()));
        }
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
    }

    @Override
    public void handleAddComponent(ColorSlider component, Container container) {
        component.setWidth(smallFontRenderer.getStringWidth(component.getText()) + smallFontRenderer.getStringWidth(component.getMaximum() + "") + 3);
        component.setHeight(component.getTheme().getFontRenderer().getFontHeight() + 2);
    }

}
