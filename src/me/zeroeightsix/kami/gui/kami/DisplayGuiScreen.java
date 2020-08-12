// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.kami;

import net.minecraft.client.Minecraft;
import me.zeroeightsix.kami.module.ModuleManager;
import org.lwjgl.input.Mouse;
import java.io.IOException;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import java.util.Iterator;
import me.zeroeightsix.kami.util.Wrapper;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.KamiMod;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.gui.GuiScreen;

public class DisplayGuiScreen extends GuiScreen
{
    KamiGUI gui;
    public final GuiScreen lastScreen;
    public static int mouseX;
    public static int mouseY;
    Framebuffer framebuffer;
    
    public DisplayGuiScreen(final GuiScreen lastScreen) {
        this.lastScreen = lastScreen;
        final KamiGUI gui = KamiMod.getInstance().getGuiManager();
        for (final Component c : gui.getChildren()) {
            if (c instanceof Frame) {
                final Frame child = (Frame)c;
                if (!child.isPinneable() || !child.isVisible()) {
                    continue;
                }
                child.setOpacity(0.5f);
            }
        }
        this.framebuffer = new Framebuffer(Wrapper.getMinecraft().field_71443_c, Wrapper.getMinecraft().field_71440_d, false);
    }
    
    public void func_146281_b() {
        final KamiGUI gui = KamiMod.getInstance().getGuiManager();
        gui.getChildren().stream().filter(component -> component instanceof Frame && component.isPinneable() && component.isVisible()).forEach(component -> component.setOpacity(0.0f));
    }
    
    public void func_73866_w_() {
        this.gui = KamiMod.getInstance().getGuiManager();
    }
    
    public void func_73863_a(final int mouseX, final int mouseY, final float partialTicks) {
        this.calculateMouse();
        this.gui.drawGUI();
        GL11.glEnable(3553);
        GlStateManager.func_179124_c(1.0f, 1.0f, 1.0f);
    }
    
    protected void func_73864_a(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        this.gui.handleMouseDown(DisplayGuiScreen.mouseX, DisplayGuiScreen.mouseY);
    }
    
    protected void func_146286_b(final int mouseX, final int mouseY, final int state) {
        this.gui.handleMouseRelease(DisplayGuiScreen.mouseX, DisplayGuiScreen.mouseY);
    }
    
    protected void func_146273_a(final int mouseX, final int mouseY, final int clickedMouseButton, final long timeSinceLastClick) {
        this.gui.handleMouseDrag(DisplayGuiScreen.mouseX, DisplayGuiScreen.mouseY);
    }
    
    public void func_73876_c() {
        if (Mouse.hasWheel()) {
            final int a = Mouse.getDWheel();
            if (a != 0) {
                this.gui.handleWheel(DisplayGuiScreen.mouseX, DisplayGuiScreen.mouseY, a);
            }
        }
    }
    
    protected void func_73869_a(final char typedChar, final int keyCode) throws IOException {
        if (ModuleManager.getModuleByName("clickGUI").getBind().isDown(keyCode) || keyCode == 1) {
            this.field_146297_k.func_147108_a(this.lastScreen);
        }
        else {
            this.gui.handleKeyDown(keyCode);
            this.gui.handleKeyUp(keyCode);
        }
    }
    
    public static int getScale() {
        int scaleFactor = 0;
        int scale = Wrapper.getMinecraft().field_71474_y.field_74335_Z;
        if (scale == 0) {
            scale = 1000;
        }
        while (scaleFactor < scale && Wrapper.getMinecraft().field_71443_c / (scaleFactor + 1) >= 320 && Wrapper.getMinecraft().field_71440_d / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }
        if (scaleFactor == 0) {
            scaleFactor = 1;
        }
        return scaleFactor;
    }
    
    private void calculateMouse() {
        final Minecraft minecraft = Minecraft.func_71410_x();
        final int scaleFactor = getScale();
        DisplayGuiScreen.mouseX = Mouse.getX() / scaleFactor;
        DisplayGuiScreen.mouseY = minecraft.field_71440_d / scaleFactor - Mouse.getY() / scaleFactor - 1;
    }
}
