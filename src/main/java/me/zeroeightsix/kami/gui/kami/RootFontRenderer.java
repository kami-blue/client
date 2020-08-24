package me.zeroeightsix.kami.gui.kami;

import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by 086 on 26/06/2017.
 */
public class RootFontRenderer implements FontRenderer {

    private final float fontsize;
    private final net.minecraft.client.gui.FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

    public RootFontRenderer(float fontsize) {
        this.fontsize = fontsize;
    }

    @Override
    public int getFontHeight() {
        return (int) (Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT * fontsize);
    }

    @Override
    public int getStringHeight(String text) {
        return getFontHeight();
    }

    @Override
    public int getStringWidth(String text) {
        return (int) (fontRenderer.getStringWidth(text) * fontsize);
    }

    @Override
    public void drawString(int x, int y, String text) {
        drawString(x, y, 255, 255, 255, text);
    }

    @Override
    public void drawString(int x, int y, int r, int g, int b, String text) {
        drawString(x, y, (0xff << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff), text);
    }

    @Override
    public void drawString(int x, int y, Color color, String text) {
        drawString(x, y, color.getRGB(), text);
    }

    @Override
    public void drawString(int x, int y, int colour, String text) {
        drawString(x, y, colour, text, true);
    }

    public void drawString(int x, int y, int colour, String text, boolean shadow) {
        prepare(x, y);
        Minecraft.getMinecraft().fontRenderer.drawString(text, 0, 0, colour, shadow);
        pop(x, y);
    }

    @Override
    public void drawStringWithShadow(int x, int y, int r, int g, int b, String text) {
        drawString(x, y, (0xff << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff), text, true);
    }

    private void prepare(int x, int y) {
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glTranslatef(x, y, 0);
        glScalef(fontsize, fontsize, 1);
        glColor4f(1, 1, 1, 1);
    }

    private void pop(int x, int y) {
        glScalef(1f / fontsize, 1f / fontsize, 1);
        glTranslatef(-x, -y, 0);
        glDisable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
    }
}
