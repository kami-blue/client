package me.zeroeightsix.kami.gui.kami;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame;
import me.zeroeightsix.kami.module.modules.ClickGUI;
import me.zeroeightsix.kami.util.Wrapper;
import me.zeroeightsix.kami.util.graphics.GlStateUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by 086 on 3/08/2017.
 * Updated by l1ving on 13/12/19
 * Updated by Xiaro on 18/08/20
 */
public class DisplayGuiScreen extends GuiScreen {

    public static int mouseX;
    public static int mouseY;
    public final GuiScreen lastScreen;
    KamiGUI gui;
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

    public static double getScale() {
        return ClickGUI.INSTANCE.getScaleFactor();
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
        GlStateUtils.rescaleKami();
        gui.drawGUI();
        GlStateUtils.rescaleMc();
        GlStateUtils.blend(false);
        glColor4f(1f, 1f, 1f, 1f);
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
    protected void keyTyped(char typedChar, int keyCode) {
        if (ClickGUI.INSTANCE.isEnabled() && (keyCode == Keyboard.KEY_ESCAPE || ClickGUI.INSTANCE.bind.getValue().isDown(keyCode))) {
            ClickGUI.INSTANCE.disable();
        } else {
            gui.handleKeyDown(keyCode);
            gui.handleKeyUp(keyCode);
        }
    }

    public void closeGui() {
        mc.displayGuiScreen(lastScreen);
    }

    private void calculateMouse() {
        Minecraft minecraft = Minecraft.getMinecraft();
        double scaleFactor = getScale();
        mouseX = (int) (Mouse.getX() / scaleFactor);
        mouseY = (int) (minecraft.displayHeight / scaleFactor - Mouse.getY() / scaleFactor - 1);
    }

}
