package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.kami.RenderHelper;
import me.zeroeightsix.kami.gui.kami.RootSmallFontRenderer;
import me.zeroeightsix.kami.gui.kami.theme.kami.KamiGuiColors.GuiC;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.component.use.Slider;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;

import static me.zeroeightsix.kami.util.ColourConverter.toF;
import static org.lwjgl.opengl.GL11.*;

/**
 * Created by 086 on 8/08/2017.
 * updated by Xiaro on 04/08/20
 */
public class RootSliderUI extends AbstractComponentUI<Slider> {

    RootSmallFontRenderer smallFontRenderer = new RootSmallFontRenderer();

    @Override
    public void renderComponent(Slider component, FontRenderer aa) {
        glColor4f(toF(GuiC.sliderColour.color.getRed()), toF(GuiC.sliderColour.color.getGreen()), toF(GuiC.sliderColour.color.getBlue()), component.getOpacity());
        glLineWidth(2.5f);
        int height = component.getHeight();
        double value = component.getValue();
        double w = component.getWidth() * ((value - component.getMinimum()) / (component.getMaximum() - component.getMinimum()));
        float downscale = 1.1f;
        glBegin(GL_LINES);
        {
            glVertex2d(0, height / downscale);
            glVertex2d(w, height / downscale);
        }
        glColor3f(0.33f, 0.33f, 0.33f);
        {
            glVertex2d(w, height / downscale);
            glVertex2d(component.getWidth(), height / downscale);
        }
        glEnd();
        glColor3f(toF(GuiC.sliderColour.color.getRed()), toF(GuiC.sliderColour.color.getGreen()), toF(GuiC.sliderColour.color.getBlue()));
        RenderHelper.drawCircle((int) w, height / downscale, 2f);

        String s = value + "";
        if (component.isPressed()) {
            w -= smallFontRenderer.getStringWidth(s) / 2f;
            w = Math.max(0, Math.min(w, component.getWidth() - smallFontRenderer.getStringWidth(s)));
            smallFontRenderer.drawString((int) w, 0, s);
        } else {
            smallFontRenderer.drawString(0, 0, component.getText());
            smallFontRenderer.drawString(component.getWidth() - smallFontRenderer.getStringWidth(s), 0, s);
        }
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
    }

    @Override
    public void handleAddComponent(Slider component, Container container) {
        component.setHeight(component.getTheme().getFontRenderer().getFontHeight() + 2);
        component.setWidth(smallFontRenderer.getStringWidth(component.getText()) + smallFontRenderer.getStringWidth(component.getMaximum() + "") + 3);
    }
}
