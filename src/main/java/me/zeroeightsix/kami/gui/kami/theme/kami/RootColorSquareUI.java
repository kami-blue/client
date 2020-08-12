package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.kami.RenderHelper;
import me.zeroeightsix.kami.gui.kami.RootSmallFontRenderer;
import me.zeroeightsix.kami.gui.kami.theme.kami.KamiGuiColors.GuiC;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.component.use.ColorSquare;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import java.awt.*;

import static me.zeroeightsix.kami.util.ColourConverter.toF;
import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Guac on 10/8/2020.
 */
public class RootColorSquareUI extends AbstractComponentUI<ColorSquare> {

    RootSmallFontRenderer smallFontRenderer = new RootSmallFontRenderer();

    @Override
    public void renderComponent(ColorSquare component, FontRenderer aa) {
        glColor4f(toF(GuiC.sliderColour.color.getRed()), toF(GuiC.sliderColour.color.getGreen()), toF(GuiC.sliderColour.color.getBlue()), component.getOpacity());
        glLineWidth(10f);
        int width = component.getWidth();
        int height = component.getHeight();
        Color value = component.getValue();
        float[] HSB = Color.RGBtoHSB(value.getRed(), value.getGreen(), value.getBlue(), null);
        double w = width * (HSB[1]);
        double v = height * (HSB[2]);

        glShadeModel(GL_SMOOTH);
        glBegin(GL_QUADS);
        glColor3f(255, 255, 255);
        glVertex2d(0, 0); //White corner
        glColor3f(value.getRed(), value.getGreen(), value.getBlue());
        glVertex2d(width, 0); //Colored corner
        glColor3f(0, 0, 0);
        glVertex2d(width, height); //Bottom right black corner
        glVertex2d(0, height); //Bottom left black corner
        glEnd();

        glColor3f(toF(GuiC.sliderColour.color.getRed()), toF(GuiC.sliderColour.color.getGreen()), toF(GuiC.sliderColour.color.getBlue()));
        RenderHelper.drawCircle((int)w, (int)v, 2f);

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
    }

    @Override
    public void handleAddComponent(ColorSquare component, Container container) {
        component.setWidth(smallFontRenderer.getStringWidth(component.getText()) + smallFontRenderer.getStringWidth(component.getxMaximum() + "") + 3);
        //component.setHeight(component.getTheme().getFontRenderer().getFontHeight() + 2);
        component.setHeight(smallFontRenderer.getStringWidth(component.getText()) + smallFontRenderer.getStringWidth(component.getxMaximum() + "") + 3);
    }

}
