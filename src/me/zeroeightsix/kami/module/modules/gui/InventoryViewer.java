// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.gui;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import net.minecraft.util.ResourceLocation;
import java.util.Iterator;
import java.util.List;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.util.ContainerHelper;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.gui.kami.KamiGUI;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "InventoryViewer", category = Category.GUI, description = "View your inventory on screen", showOnArray = ShowOnArray.OFF)
public class InventoryViewer extends Module
{
    public Setting<Boolean> startupGlobal;
    private Setting<ViewMode> viewMode;
    KamiGUI kamiGUI;
    
    public InventoryViewer() {
        this.startupGlobal = this.register(Settings.b("Enable Automatically", true));
        this.viewMode = this.register(Settings.e("Appearance", ViewMode.ICONLARGE));
        this.kamiGUI = KamiMod.getInstance().getGuiManager();
    }
    
    private int invPos(final int i) {
        this.kamiGUI = KamiMod.getInstance().getGuiManager();
        if (this.kamiGUI != null) {
            final List<Frame> frames = ContainerHelper.getAllChildren((Class<? extends Frame>)Frame.class, (Container)this.kamiGUI);
            for (final Frame frame : frames) {
                if (!frame.getTitle().equalsIgnoreCase("inventory viewer")) {
                    continue;
                }
                switch (i) {
                    case 0: {
                        return frame.getX();
                    }
                    case 1: {
                        return frame.getY();
                    }
                    default: {
                        return 0;
                    }
                }
            }
        }
        return 0;
    }
    
    private ResourceLocation getBox() {
        if (this.viewMode.getValue().equals(ViewMode.CLEAR)) {
            return new ResourceLocation("textures/gui/container/invpreview.png");
        }
        if (this.viewMode.getValue().equals(ViewMode.ICONBACK)) {
            return new ResourceLocation("textures/gui/container/one.png");
        }
        if (this.viewMode.getValue().equals(ViewMode.KONC)) {
            return new ResourceLocation("textures/gui/container/konc.png");
        }
        if (this.viewMode.getValue().equals(ViewMode.BD)) {
            return new ResourceLocation("textures/gui/container/bd.png");
        }
        if (this.viewMode.getValue().equals(ViewMode.SOLID)) {
            return new ResourceLocation("textures/gui/container/two.png");
        }
        if (this.viewMode.getValue().equals(ViewMode.SOLIDCLEAR)) {
            return new ResourceLocation("textures/gui/container/three.png");
        }
        if (this.viewMode.getValue().equals(ViewMode.ICON)) {
            return new ResourceLocation("textures/gui/container/four.png");
        }
        if (this.viewMode.getValue().equals(ViewMode.ICONLARGE)) {
            return new ResourceLocation("textures/gui/container/five.png");
        }
        if (this.viewMode.getValue().equals(ViewMode.ICONLARGEBG)) {
            return new ResourceLocation("textures/gui/container/six.png");
        }
        return new ResourceLocation("textures/gui/container/generic_54.png");
    }
    
    private static void preBoxRender() {
        GL11.glPushMatrix();
        GlStateManager.func_179094_E();
        GlStateManager.func_179118_c();
        GlStateManager.func_179086_m(256);
        GlStateManager.func_179147_l();
    }
    
    private static void postBoxRender() {
        GlStateManager.func_179084_k();
        GlStateManager.func_179097_i();
        GlStateManager.func_179140_f();
        GlStateManager.func_179126_j();
        GlStateManager.func_179141_d();
        GlStateManager.func_179121_F();
        GL11.glPopMatrix();
    }
    
    private static void preItemRender() {
        GL11.glPushMatrix();
        GL11.glDepthMask(true);
        GlStateManager.func_179086_m(256);
        GlStateManager.func_179097_i();
        GlStateManager.func_179126_j();
        RenderHelper.func_74519_b();
        GlStateManager.func_179152_a(1.0f, 1.0f, 0.01f);
    }
    
    private static void postItemRender() {
        GlStateManager.func_179152_a(1.0f, 1.0f, 1.0f);
        RenderHelper.func_74518_a();
        GlStateManager.func_179141_d();
        GlStateManager.func_179084_k();
        GlStateManager.func_179140_f();
        GlStateManager.func_179139_a(0.5, 0.5, 0.5);
        GlStateManager.func_179097_i();
        GlStateManager.func_179126_j();
        GlStateManager.func_179152_a(2.0f, 2.0f, 2.0f);
        GL11.glPopMatrix();
    }
    
    @Override
    public void onRender() {
        final NonNullList<ItemStack> items = (NonNullList<ItemStack>)InventoryViewer.mc.field_71439_g.field_71071_by.field_70462_a;
        this.boxRender(this.invPos(0), this.invPos(1));
        this.itemRender(items, this.invPos(0), this.invPos(1));
    }
    
    private void boxRender(final int x, final int y) {
        preBoxRender();
        final ResourceLocation box = this.getBox();
        InventoryViewer.mc.field_71446_o.func_110577_a(box);
        InventoryViewer.mc.field_71456_v.func_73729_b(x, y, 7, 17, 162, 54);
        postBoxRender();
    }
    
    private void itemRender(final NonNullList<ItemStack> items, final int x, final int y) {
        for (int size = items.size(), item = 9; item < size; ++item) {
            final int slotX = x + 1 + item % 9 * 18;
            final int slotY = y + 1 + (item / 9 - 1) * 18;
            preItemRender();
            InventoryViewer.mc.func_175599_af().func_180450_b((ItemStack)items.get(item), slotX, slotY);
            InventoryViewer.mc.func_175599_af().func_175030_a(InventoryViewer.mc.field_71466_p, (ItemStack)items.get(item), slotX, slotY);
            postItemRender();
        }
    }
    
    private enum ViewMode
    {
        ICONLARGEBG, 
        ICONLARGE, 
        MC, 
        ICON, 
        ICONBACK, 
        CLEAR, 
        SOLID, 
        SOLIDCLEAR, 
        AstraMod, 
        BD;
    }
}
