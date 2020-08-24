package me.zeroeightsix.kami.gui.kami.theme.kami;

import it.unimi.dsi.fastutil.floats.FloatAVLTreeSet;
import me.zeroeightsix.kami.gui.kami.RenderHelper;
import me.zeroeightsix.kami.gui.kami.theme.kami.KamiGuiColors.GuiC;
import me.zeroeightsix.kami.gui.rgui.component.use.ColorSquare;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.modules.render.ESP;
import me.zeroeightsix.kami.util.HSBColourHolder;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static me.zeroeightsix.kami.util.ColourConverter.toF;
import static net.minecraft.client.renderer.GlStateManager.glBlendEquation;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.GL_FUNC_ADD;

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
        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_DST_COLOR, GL_SRC_ALPHA); // not sure this is right
        glBegin(GL_QUADS);
        glColor4f(1f, 1f, 1f, 1f);
        glVertex2d(0, 0); // White corner
        glColor4f(0f, 0f, 0f, 1f);
        glVertex2d(0, height); // Bottom left black corner
        glVertex2d(width, height); // Bottom right black corner
        glColor3f(1f, 1f, 1f);
        glVertex2d(width, 0);
        // new quad
        glColor4f(1f, 1f, 1f, 0f);
        glVertex2d(0, 0); // zero saturation side
        glColor4f(0f, 0f, 0f, 0f);
        glVertex2d(0, height); // Bottom left black corner
        glColor4f(0f, 0f, 0f, 1f);
        glVertex2d(width, height); // Bottom right black corner
        glColor3f(toF(raw.getRed()), toF(raw.getGreen()), toF(raw.getBlue()));
        glVertex2d(width, 0); // Full saturation side
        glEnd();

        glColor3f(toF(GuiC.sliderColour.color.getRed()), toF(GuiC.sliderColour.color.getGreen()), toF(GuiC.sliderColour.color.getBlue()));
        RenderHelper.drawCircle(w, v, 2f);

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
    }
}
