package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.kami.RootSmallFontRenderer;
import me.zeroeightsix.kami.gui.kami.component.EnumButton;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;

import java.awt.*;

import static me.zeroeightsix.kami.gui.kami.theme.kami.KamiGuiColors.GuiC;
import static me.zeroeightsix.kami.util.color.ColorConverter.toF;
import static org.lwjgl.opengl.GL11.*;

/**
 * Created by 086 on 8/08/2017.
 */
public class KamiEnumButtonUI extends AbstractComponentUI<EnumButton> {

    RootSmallFontRenderer smallFontRenderer = new RootSmallFontRenderer();

    protected Color idleColour = new Color(163, 163, 163);
    protected Color downColour = new Color(255, 255, 255);

    EnumButton modeComponent;
    long lastMS = System.currentTimeMillis();

    @Override
    public void renderComponent(EnumButton component, FontRenderer aa) {
        if (System.currentTimeMillis() - lastMS > 3000 && modeComponent != null) {
            modeComponent = null;
        }

        int c = component.isPressed() ? 0xaaaaaa : 0xdddddd;
        if (component.isHovered())
            c = (c & 0x7f7f7f) << 1;

        //RenderHelper.drawRoundedRectangle(0,0,component.getWidth(), component.getHeight(), 3f);

        glColor3f(1, 1, 1);
        glEnable(GL_TEXTURE_2D);

        int parts = component.getModes().length;
        double step = component.getWidth() / (double) parts;
        double startX = step * component.getIndex();
        double endX = step * (component.getIndex() + 1);

        int height = component.getHeight();
        float downscale = 1.1f;

        glDisable(GL_TEXTURE_2D);
        glColor3f(toF(GuiC.enumColour.color.getRed()), toF(GuiC.enumColour.color.getGreen()), toF(GuiC.enumColour.color.getBlue())); // SEARCHCOLOUR: Enum and Bind Colours
        glBegin(GL_LINES);
        {
            glVertex2d(startX, height / downscale);
            glVertex2d(endX, height / downscale);
        }
        glEnd();

        if (modeComponent == null || !modeComponent.equals(component)) {
            smallFontRenderer.drawString(0, 0, c, component.getName());
            smallFontRenderer.drawString(component.getWidth() - smallFontRenderer.getStringWidth(component.getIndexMode()), 0, c, component.getIndexMode());
        } else {
            smallFontRenderer.drawString(component.getWidth() / 2 - smallFontRenderer.getStringWidth(component.getIndexMode()) / 2, 0, c, component.getIndexMode());
        }
    }

    @Override
    public void handleSizeComponent(EnumButton component) {
        int width = 0;
        for (String s : component.getModes()) {
            width = Math.max(width, smallFontRenderer.getStringWidth(s));
        }
        component.setWidth(smallFontRenderer.getStringWidth(component.getName()) + width + 1);
        component.setHeight(smallFontRenderer.getFontHeight() + 2);
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
