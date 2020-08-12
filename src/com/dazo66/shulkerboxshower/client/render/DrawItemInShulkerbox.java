// 
// Decompiled by Procyon v0.5.36
// 

package com.dazo66.shulkerboxshower.client.render;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.GlStateManager;
import java.util.Iterator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.util.NonNullList;
import java.util.ArrayList;
import com.dazo66.shulkerboxshower.ShulkerBoxViewer;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.Minecraft;

public class DrawItemInShulkerbox
{
    public int x;
    public int y;
    private Minecraft mc;
    private static final ResourceLocation GUI_TEXTURE;
    
    public DrawItemInShulkerbox() {
        this.x = 0;
        this.y = 0;
        this.mc = Minecraft.func_71410_x();
    }
    
    public void draw(final GuiScreen gui, final ItemStack itemStack) {
        final List<ItemStack> list = this.arrangementItem(itemStack);
        if (!list.isEmpty()) {
            this.drawItemStack(gui, list, this.x + 4, this.y - 100);
        }
    }
    
    public void draw(final GuiScreen gui, final ItemStack itemStack, final ItemStack itemStack1, final int x, final int y) {
        final List<ItemStack> list = this.arrangementItem(itemStack);
        if (!list.isEmpty()) {
            final int size = list.size();
            int i = size / 9 + ((size % 9 != 0) ? 1 : 0);
            if (!ShulkerBoxViewer.config.isOrganizing()) {
                i = 3;
            }
            this.drawItemStack(gui, list, x + 7, y - 110 - 18 + 42 + i * 18);
        }
        final List<ItemStack> list2 = this.arrangementItem(itemStack1);
        if (!list2.isEmpty()) {
            this.drawItemStack(gui, list2, x + 7, y - 100);
        }
    }
    
    private List<ItemStack> arrangementItem(final ItemStack itemStack) {
        final List<ItemStack> list = new ArrayList<ItemStack>();
        final NBTTagCompound nbttagcompound = itemStack.func_77978_p();
        if (nbttagcompound != null && nbttagcompound.func_150297_b("BlockEntityTag", 10)) {
            final NBTTagCompound blockEntityTag = nbttagcompound.func_74775_l("BlockEntityTag");
            if (blockEntityTag.func_150297_b("Items", 9)) {
                final NonNullList<ItemStack> nonNullList = (NonNullList<ItemStack>)NonNullList.func_191197_a(27, (Object)ItemStack.field_190927_a);
                ItemStackHelper.func_191283_b(blockEntityTag, (NonNullList)nonNullList);
                if (!ShulkerBoxViewer.config.isOrganizing()) {
                    return (List<ItemStack>)nonNullList;
                }
                for (final ItemStack itemStack2 : nonNullList) {
                    if (itemStack2.func_190926_b()) {
                        continue;
                    }
                    boolean flag = true;
                    for (final ItemStack itemStack3 : list) {
                        if (itemStack2.func_77969_a(itemStack3) && ItemStack.func_77970_a(itemStack3, itemStack2)) {
                            itemStack3.func_190920_e(itemStack2.func_190916_E() + itemStack3.func_190916_E());
                            flag = false;
                        }
                    }
                    if (!flag) {
                        continue;
                    }
                    list.add(itemStack2);
                }
            }
        }
        return list;
    }
    
    private void drawItemStack(final GuiScreen gui, final List<ItemStack> list, final int x, final int y) {
        GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.func_179084_k();
        final int i2;
        int i = i2 = list.size() / 9 + ((list.size() % 9 != 0) ? 1 : 0);
        if (i2 == 3) {
            i = 1;
        }
        else if (i2 == 1) {
            i = 3;
        }
        this.mc.func_110434_K().func_110577_a(DrawItemInShulkerbox.GUI_TEXTURE);
        GlStateManager.func_179097_i();
        GlStateManager.func_179140_f();
        gui.func_73729_b(x - 8, y + 12 + i * 18, 0, 0, 176, 5);
        gui.func_73729_b(x - 8, y + 12 + i * 18 + 5, 0, 16, 176, i2 * 18);
        gui.func_73729_b(x - 8, y + 17 + i * 18 + i2 * 18, 0, 160, 176, 6);
        GlStateManager.func_179126_j();
        GlStateManager.func_179109_b(0.0f, 0.0f, 32.0f);
        for (int size = list.size(), l = 0; l < size; ++l) {
            this.drawItemStack(this.mc.func_175599_af(), list.get(l), l % 9 * 18 + x, i * 18 + (l / 9 + 1) * 18 + y + 1);
        }
        GlStateManager.func_179140_f();
        this.mc.func_175599_af().field_77023_b = 0.0f;
    }
    
    private void drawItemStack1(final List<ItemStack> nonNullList, final int x, final int y) {
        final RenderItem itemRender = this.mc.func_175599_af();
        GlStateManager.func_179109_b(0.0f, 0.0f, 32.0f);
        for (int k = 0; k < 3; ++k) {
            for (int l = 0; l < 9; ++l) {
                this.drawItemStack(itemRender, nonNullList.get(l + k * 9), 8 + l * 18 + x - 15, 18 + k * 18 + y - 35);
            }
        }
        GlStateManager.func_179140_f();
    }
    
    private void drawItemStack(final RenderItem itemRender, final ItemStack stack, final int x, final int y) {
        FontRenderer font = stack.func_77973_b().getFontRenderer(stack);
        if (font == null) {
            font = this.mc.field_71466_p;
        }
        GlStateManager.func_179126_j();
        itemRender.field_77023_b = 120.0f;
        RenderHelper.func_74520_c();
        itemRender.func_180450_b(stack, x, y);
        final String count = (stack.func_190916_E() == 1) ? "" : String.valueOf(stack.func_190916_E());
        final String more = (stack.func_190916_E() % stack.func_77976_d() == 0) ? "" : ("+" + stack.func_190916_E() % stack.func_77976_d());
        final String count2 = (stack.func_190916_E() == 1) ? "" : (stack.func_190916_E() / stack.func_77976_d() + "S" + more);
        itemRender.func_180453_a(font, stack, x, y, count);
        itemRender.field_77023_b = 0.0f;
    }
    
    static {
        GUI_TEXTURE = new ResourceLocation("textures/gui/container/shulker_box.png");
    }
}
