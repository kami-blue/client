package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.kami.RenderHelper;
import me.zeroeightsix.kami.gui.kami.theme.kami.KamiGuiColors.GuiC;
import me.zeroeightsix.kami.gui.rgui.component.use.ColorSquare;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.util.HSBColourHolder;

import static me.zeroeightsix.kami.util.ColourConverter.toF;
import static net.minecraft.client.renderer.GlStateManager.glBlendEquation;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.GL_FUNC_ADD;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;

/**
 * Created by Guac on 10/8/2020.
 */
public class RootColorSquareUI extends AbstractComponentUI<ColorSquare> {
    @Override
    public void renderComponent(ColorSquare component, FontRenderer aa) {
        int width = component.getWidth();
        int height = component.getHeight();
        HSBColourHolder value = component.getValue();
        HSBColourHolder raw = value.getRaw();
        float w = width * (value.getS());
        float v = height * (1 - value.getB());

        glShadeModel(GL_SMOOTH);
        glEnable(GL_BLEND);
        glDisable(GL_ALPHA_TEST);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE);
        glBlendEquation(GL_FUNC_ADD);

        glBegin(GL_QUADS); // Haha get played GL
        // First Quad
        glColor4f(1f, 1f, 1f, 1f); // Left
        glVertex2d(0, 0); // top-left
        glVertex2d(0, height); // bottom-left
        glColor3f(toF(raw.getRed()), toF(raw.getGreen()), toF(raw.getBlue())); // Right
        glVertex2d(width, height); // bottom-right
        glVertex2d(width, 0); // top-right
        //Second Quad
        glColor4f(0f, 0f, 0f, 0f); // Top
        glVertex2d(width, 0); // top-right
        glVertex2d(0, 0); // top-left
        glColor4f(0f, 0f, 0f, 1f); // Bottom
        glVertex2d(0, height); // bottom-left
        glVertex2d(width, height); // bottom-right
        glEnd();

        // Outline
        /*glLineWidth(1.5f);
        glBegin(GL_LINE_LOOP);
        glColor3f(.60f, .56f, 1.00f);
        glVertex2d(width, 0); // top-right
        glVertex2d(0, 0); // top-leftBottom
        glVertex2d(0, height); // bottom-left
        glVertex2d(width, height); // bottom-right
        glEnd();*/

        glColor3f(toF(GuiC.sliderColour.color.getRed()), toF(GuiC.sliderColour.color.getGreen()), toF(GuiC.sliderColour.color.getBlue()));
        RenderHelper.drawCircle(w, v, 2f);

        glEnable(GL_ALPHA_TEST);
        glDisable(GL_BLEND);
    }
}
