package me.zeroeightsix.kami.module.modules.gui;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.gui.kami.KamiGUI;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame;
import me.zeroeightsix.kami.gui.rgui.util.ContainerHelper;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.KamiTessellator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/***
 * Updated by S-B99 on 18/02/20
 * Everything except somethingRender() methods was written by S-B99
 */
@Module.Info(name = "InventoryViewer", category = Module.Category.GUI, description = "View your inventory on screen", showOnArray = Module.ShowOnArray.OFF)
public class InventoryViewer extends Module {
    private Setting<ViewSize> viewSizeSetting = register(Settings.e("Icon Size", ViewSize.MEDIUM));
    private Setting<Boolean> showIcon = register(Settings.b("Show Icon", true));
    private Setting<Boolean> colorBackground = register(Settings.b("Colored Background", true));
    private Setting<Integer> a = register(Settings.integerBuilder("Transparency").withMinimum(0).withValue(32).withMaximum(255).build());
    private Setting<Integer> r = register(Settings.integerBuilder("Red").withMinimum(0).withValue(155).withMaximum(255).build());
    private Setting<Integer> g = register(Settings.integerBuilder("Green").withMinimum(0).withValue(144).withMaximum(255).build());
    private Setting<Integer> b = register(Settings.integerBuilder("Blue").withMinimum(0).withValue(255).withMaximum(255).build());

    private boolean isLeft = false;
    private boolean isRight = false;
    private boolean isTop = false;
    private boolean isBottom = false;

    KamiGUI kamiGUI = KamiMod.getInstance().getGuiManager();
    private int invPos(int i) {
        kamiGUI = KamiMod.getInstance().getGuiManager();
        if (kamiGUI != null) {
            List<Frame> frames = ContainerHelper.getAllChildren(Frame.class, kamiGUI);
            for (Frame frame : frames) {
                if (!frame.getTitle().equalsIgnoreCase("inventory viewer")) continue;
                switch (i) {
                    case 0:
                        return frame.getX();
                    case 1:
                        return frame.getY();
                    case 3:
                        if (frame.isPinned()) return 1; // wow this is fucking horrendous
                        else return 0;
                    default:
                        return 0;

                }
            }
        }
        return 0;
    }

    private int invMoveHorizontal() {
        if (isLeft) return 45;
        if (isRight) return -45;
        return 0;
    }

    private int invMoveVertical() {
        if (isTop) return 10;
        if (isBottom) return -10;
        return 0;
    }

    private void updatePos() {
        kamiGUI = KamiMod.getInstance().getGuiManager();
        if (kamiGUI != null) {
            List<Frame> frames = ContainerHelper.getAllChildren(Frame.class, kamiGUI);
            for (Frame frame : frames) {
                if (!frame.getTitle().equalsIgnoreCase("inventory viewer")) continue;
                isTop = frame.getDocking().isTop();
                isLeft = frame.getDocking().isLeft();
                isRight = frame.getDocking().isRight();
                isBottom = frame.getDocking().isBottom();
            }
        }
    }
    private ResourceLocation getBox() {
        if (!showIcon.getValue()) {
            return new ResourceLocation("kamiblue/clear.png");
        } else if (viewSizeSetting.getValue().equals(ViewSize.LARGE)) {
            return new ResourceLocation("kamiblue/large.png");
        } else if (viewSizeSetting.getValue().equals(ViewSize.SMALL)) {
            return new ResourceLocation("kamiblue/small.png");
        } else {
            return new ResourceLocation("kamiblue/medium.png");
        }
    }

    private enum ViewSize {
        LARGE, MEDIUM, SMALL
    }

    private void colourRender(final int x, final int y) {
        if (colorBackground.getValue()) { // 1 == 2 px in game
//            glAttrib(GL_ALL_ATTRIB_BITS);
            KamiTessellator.prepare(GL11.GL_QUADS);
            KamiTessellator.drawRectangle((x + 162), (y + 54), x, y, new Color(r.getValue(), g.getValue(), b.getValue(), a.getValue()).getRGB());
            KamiTessellator.release();
//            glPopAttrib();
        }
    }
    private void boxRender(final int x, final int y) {
        preBoxRender();
        ResourceLocation box = getBox();
        mc.renderEngine.bindTexture(box);
        updatePos();
        mc.ingameGUI.drawTexturedModalRect(x, y, invMoveHorizontal() + 7, invMoveVertical() + 17, 162, 54); // 164 56 // width and height of inventory
        postBoxRender();
    }

    @Override
    public void onRender() {
        if (invPos(3) == 1) {
            final NonNullList<ItemStack> items = InventoryViewer.mc.player.inventory.mainInventory;
            colourRender(invPos(0), invPos(1));
            boxRender(invPos(0), invPos(1));
            itemRender(items, invPos(0), invPos(1));
        }
    }

    private void itemRender(final NonNullList<ItemStack> items, final int x, final int y) {
        for (int size = items.size(), item = 9; item < size; ++item) {
            final int slotX = x + 1 + item % 9 * 18;
            final int slotY = y + 1 + (item / 9 - 1) * 18;
            preItemRender();
            mc.getRenderItem().renderItemAndEffectIntoGUI(items.get(item), slotX, slotY);
            mc.getRenderItem().renderItemOverlays(mc.fontRenderer, items.get(item), slotX, slotY);
            postItemRender();
        }
    }

    private static void preBoxRender() {
//        GL11.glPushMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.disableAlpha();
        GlStateManager.clear(GL_DEPTH_BUFFER_BIT);
        GlStateManager.enableBlend();
    }

    private static void postBoxRender() {
        GlStateManager.disableBlend();
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();
//        GL11.glPopMatrix();
    }

    private static void preItemRender() {
//        GL11.glPushMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.clear(GL_DEPTH_BUFFER_BIT);
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.scale(1.0f, 1.0f, 0.01f);
    }

    private static void postItemRender() {
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
        GlStateManager.scale(2.0f, 2.0f, 2.0f);
        GlStateManager.popMatrix();
//        GL11.glPopMatrix();
    }

    @Override
    public void onDisable() { this.enable(); }
}
