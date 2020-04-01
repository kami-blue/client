package me.zeroeightsix.kami.gui.kami;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame;
import me.zeroeightsix.kami.module.modules.ClickGUI;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glEnable;

/**
 * Created by 086 on 3/08/2017.
 * Updated by S-B99 on 13/12/19
 */
public class DisplayGuiScreen extends GuiScreen {

    KamiGUI gui;
    public final GuiScreen lastScreen;

    public static int mouseX;
    public static int mouseY;

    Framebuffer framebuffer;

    public DisplayGuiScreen(GuiScreen lastScreen) {
        this.lastScreen = lastScreen;

        KamiGUI gui = KamiMod.getInstance().getGuiManager();

        for (Component c : gui.getChildren()) {
            if (c instanceof Frame) {
                Frame child = (Frame) c;
                if (child.isPinnable() && child.isVisible()) {
                    child.setOpacity(.5f);
                }
            }
        }

        framebuffer = new Framebuffer(Wrapper.getMinecraft().displayWidth, Wrapper.getMinecraft().displayHeight, false);
    }

    @Override
    public void onGuiClosed() {
        KamiGUI gui = KamiMod.getInstance().getGuiManager();

        gui.getChildren().stream().filter(component -> (component instanceof Frame) && (((Frame) component).isPinnable()) && component.isVisible()).forEach(component -> component.setOpacity(0f));
    }

    @Override
    public void initGui() {
        gui = KamiMod.getInstance().getGuiManager();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        calculateMouse();
        gui.drawGUI();
        glEnable(GL_TEXTURE_2D);
        GlStateManager.color(1, 1, 1);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        gui.handleMouseDown(DisplayGuiScreen.mouseX, DisplayGuiScreen.mouseY);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        gui.handleMouseRelease(DisplayGuiScreen.mouseX, DisplayGuiScreen.mouseY);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        gui.handleMouseDrag(DisplayGuiScreen.mouseX, DisplayGuiScreen.mouseY);
    }

    @Override
    public void updateScreen() {
        if (Mouse.hasWheel()) {
            int a = Mouse.getDWheel();
            if (a != 0) {
                gui.handleWheel(mouseX, mouseY, a);
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (MODULE_MANAGER.getModule(ClickGUI.class).getBind().isDown(keyCode) || keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(lastScreen);
        } else {
            gui.handleKeyDown(keyCode);
            gui.handleKeyUp(keyCode);
        }
    }

    public static int getScale() {
        int scaleFactor = 0;
        int scale = Wrapper.getMinecraft().gameSettings.guiScale;
        if (scale == 0)
            scale = 1000;
        while (scaleFactor < scale && Wrapper.getMinecraft().displayWidth / (scaleFactor + 1) >= 320 && Wrapper.getMinecraft().displayHeight / (scaleFactor + 1) >= 240)
            scaleFactor++;
        if (scaleFactor == 0)
            scaleFactor = 1;
        return scaleFactor;
    }

    private void calculateMouse() {
        Minecraft minecraft = Minecraft.getMinecraft();
        int scaleFactor = getScale();
        mouseX = Mouse.getX() / scaleFactor;
        mouseY = minecraft.displayHeight / scaleFactor - Mouse.getY() / scaleFactor - 1;
    }

}
