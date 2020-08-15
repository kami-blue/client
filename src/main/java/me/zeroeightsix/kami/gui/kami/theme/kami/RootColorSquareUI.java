package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.kami.RenderHelper;
import me.zeroeightsix.kami.gui.kami.theme.kami.KamiGuiColors.GuiC;
import me.zeroeightsix.kami.gui.rgui.component.use.ColorSquare;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.util.HSBColourHolder;

import static me.zeroeightsix.kami.util.ColourConverter.toF;
import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Guac on 10/8/2020.
 */
public class RootColorSquareUI extends AbstractComponentUI<ColorSquare> {
    @Override
    public void renderComponent(ColorSquare component, FontRenderer aa) {
        glColor4f(toF(GuiC.sliderColour.color.getRed()), toF(GuiC.sliderColour.color.getGreen()), toF(GuiC.sliderColour.color.getBlue()), component.getOpacity());
        int width = component.getWidth();
        int height = component.getHeight();
        HSBColourHolder value = component.getValue();
        HSBColourHolder raw = value.getRaw();
        float w = width * (value.getS());
        float v = height * (1 - value.getB());

        glShadeModel(GL_SMOOTH);
        glBegin(GL_QUADS);
            glColor3f(1f, 1f, 1f);
            glVertex2d(0, 0); //White corner
            glColor3f(0f, 0f, 0f);
            glVertex2d(0, height); //Bottom left black corner
            glVertex2d(width, height); //Bottom right black corner
            glColor3f(toF(raw.getRed()), toF(raw.getGreen()), toF(raw.getBlue()));
            glVertex2d(width, 0); //Colored corner
        glEnd();

        glColor3f(toF(GuiC.sliderColour.color.getRed()), toF(GuiC.sliderColour.color.getGreen()), toF(GuiC.sliderColour.color.getBlue()));
        RenderHelper.drawCircle(w, v, 2f);

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
    }
}
