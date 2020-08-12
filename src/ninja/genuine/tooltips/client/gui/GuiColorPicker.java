// 
// Decompiled by Procyon v0.5.36
// 

package ninja.genuine.tooltips.client.gui;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import java.io.IOException;
import ninja.genuine.tooltips.client.render.RenderHelper;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.gui.ScaledResolution;
import java.awt.Color;
import org.lwjgl.BufferUtils;
import java.nio.IntBuffer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiScreen;

public class GuiColorPicker extends GuiScreen
{
    private static final String display = "Pick a color";
    private GuiScreen parent;
    private GuiTextField text;
    private int hue;
    private int color;
    private int sliderPos;
    private int x;
    private int y;
    private int hueWidth;
    private int width;
    private int height;
    private IntBuffer colorBuffer;
    
    public GuiColorPicker(final GuiScreen parent, final GuiTextField text, final String defaultText) {
        this.hueWidth = 10;
        this.width = 128;
        this.height = 128;
        this.colorBuffer = BufferUtils.createIntBuffer(4);
        this.parent = parent;
        this.text = text;
        try {
            this.color = Integer.decode(text.func_146179_b());
        }
        catch (NumberFormatException e) {
            try {
                this.color = Integer.decode(defaultText);
            }
            catch (NumberFormatException e2) {
                this.color = 65280;
            }
        }
        float[] hsb = new float[3];
        hsb = Color.RGBtoHSB(this.color >> 16 & 0xFF, this.color >> 8 & 0xFF, this.color & 0xFF, hsb);
        hsb[1] = (hsb[2] = 1.0f);
        this.hue = Color.HSBtoRGB(hsb[0], 1.0f, 1.0f);
    }
    
    public void func_73866_w_() {
        final ScaledResolution sr = new ScaledResolution(this.field_146297_k);
        this.func_189646_b(new GuiButton(0, sr.func_78326_a() / 2 - 100, sr.func_78328_b() - 30, I18n.func_135052_a("gui.cancel", new Object[0])));
        this.func_189646_b(new GuiButton(1, sr.func_78326_a() / 2 - 100, sr.func_78328_b() - 55, I18n.func_135052_a("gui.done", new Object[0])));
        this.x = sr.func_78326_a() / 2 - (this.width + this.hueWidth);
        this.y = sr.func_78328_b() / 2 - this.height / 2 - 20;
    }
    
    public void func_73863_a(final int mouseX, final int mouseY, final float partialTicks) {
        final ScaledResolution sr = new ScaledResolution(this.field_146297_k);
        this.func_146276_q_();
        this.func_73733_a(this.x - 2, this.y - 1, this.x + this.width * 2 + this.hueWidth + 2, this.y + this.height + 1, -12566464, -12566464);
        RenderHelper.drawHuePicker(this.x, this.y, this.field_73735_i, this.hueWidth, this.height);
        RenderHelper.drawColorPicker(this.x + this.hueWidth, this.y, this.field_73735_i, this.width, this.height, this.hue);
        this.func_73733_a(this.x + this.width + this.hueWidth + 1, this.y, this.x + this.width * 2 + this.hueWidth + 1, this.y + this.height, this.color | 0xFF000000, this.color | 0xFF000000);
        this.field_146289_q.func_78276_b("Pick a color", sr.func_78326_a() / 2 - this.field_146289_q.func_78256_a("Pick a color") / 2, this.y - 26, -1);
        super.func_73863_a(mouseX, mouseY, partialTicks);
    }
    
    protected void func_73864_a(final int mouseX, final int mouseY, final int clickedMouseButton) throws IOException {
        super.func_73864_a(mouseX, mouseY, clickedMouseButton);
        this.mouseClick(mouseX, mouseY, clickedMouseButton);
    }
    
    protected void func_146273_a(final int mouseX, final int mouseY, final int clickedMouseButton, final long timeSinceLastClick) {
        super.func_146273_a(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        this.mouseClick(mouseX, mouseY, clickedMouseButton);
    }
    
    private void mouseClick(final int mouseX, final int mouseY, final int clickedMouseButton) {
        if (clickedMouseButton != 0) {
            return;
        }
        GL11.glFlush();
        GL11.glFinish();
        GL11.glReadBuffer(1029);
        GL11.glReadPixels(Mouse.getX(), Mouse.getY(), 1, 1, 6408, 5124, this.colorBuffer);
        final int[] cl = new int[4];
        this.colorBuffer.get(cl);
        final int tmp = (cl[2] / 128 & 0xFF) | (cl[1] / 128 & 0xFF00) | (cl[0] / 128 & 0xFF0000) | 0xFF000000;
        if (mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.hueWidth && mouseY < this.y + this.height) {
            this.hue = (tmp & 0xFFFFFF);
            this.sliderPos = mouseY;
        }
        else if (mouseX >= this.x + this.hueWidth + 1 && mouseY >= this.y && mouseX < this.x + this.width + this.hueWidth && mouseY < this.y + this.height) {
            this.color = (tmp & 0xFFFFFF);
        }
        this.colorBuffer.clear();
    }
    
    protected void func_146284_a(final GuiButton button) throws IOException {
        if (button.field_146127_k == 0) {
            this.field_146297_k.func_147108_a(this.parent);
        }
        else if (button.field_146127_k == 1) {
            this.text.func_146180_a("0x" + Integer.toHexString(this.color).toUpperCase());
            this.field_146297_k.func_147108_a(this.parent);
        }
    }
}
