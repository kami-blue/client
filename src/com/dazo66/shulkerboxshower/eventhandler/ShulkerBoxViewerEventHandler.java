// 
// Decompiled by Procyon v0.5.36
// 

package com.dazo66.shulkerboxshower.eventhandler;

import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraft.inventory.Slot;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.Iterator;
import java.util.List;
import net.minecraft.util.text.translation.I18n;
import java.util.ArrayList;
import net.minecraft.item.ItemShulkerBox;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraft.client.Minecraft;
import com.dazo66.shulkerboxshower.client.render.DrawItemInShulkerbox;

public class ShulkerBoxViewerEventHandler
{
    public static ShulkerBoxViewerEventHandler instance;
    private DrawItemInShulkerbox drawer;
    private Minecraft mc;
    
    public ShulkerBoxViewerEventHandler() {
        this.drawer = new DrawItemInShulkerbox();
        this.mc = Minecraft.func_71410_x();
    }
    
    @SubscribeEvent
    public void onTooltipGen(final ItemTooltipEvent event) {
        if (event.getItemStack().func_77973_b() instanceof ItemShulkerBox) {
            final List<String> list = (List<String>)event.getToolTip();
            final List<String> temp = new ArrayList<String>();
            for (final String s : list) {
                if (s.matches("^.*\\sx\\d+$")) {
                    temp.add(s);
                }
            }
            for (final String s : temp) {
                list.remove(s);
            }
            if (list.size() < 2) {
                return;
            }
            final String[] strings = I18n.func_74838_a("container.shulkerBox.more").split("%s");
            if (list.get(1).contains(strings[0]) && list.get(1).contains(strings[1])) {
                list.remove(1);
            }
        }
    }
    
    @SubscribeEvent
    public void afterDrawGui(final GuiScreenEvent.DrawScreenEvent.Post event) {
        if (event.getGui() instanceof GuiContainer) {
            final GuiContainer gui = (GuiContainer)event.getGui();
            final Slot slotUnderMouse = gui.getSlotUnderMouse();
            final ItemStack itemInHand = this.mc.field_71439_g.field_71071_by.func_70445_o();
            if (null == slotUnderMouse) {
                if (!itemInHand.func_190926_b() && itemInHand.func_77973_b() instanceof ItemShulkerBox) {
                    this.drawer.draw((GuiScreen)gui, itemInHand, ItemStack.field_190927_a, event.getMouseX() + 10, event.getMouseY());
                }
            }
            else if (slotUnderMouse.func_75216_d()) {
                final ItemStack itemUnderMouse = slotUnderMouse.func_75211_c();
                if (itemUnderMouse.func_77973_b() instanceof ItemShulkerBox) {
                    final boolean flag = !itemInHand.func_190926_b() && itemInHand.func_77973_b() instanceof ItemShulkerBox;
                    if (flag) {
                        this.drawer.draw((GuiScreen)gui, itemInHand, itemUnderMouse, event.getMouseX() + 10, event.getMouseY());
                    }
                    else {
                        this.drawer.draw((GuiScreen)gui, itemUnderMouse);
                    }
                }
                else if (itemInHand.func_77973_b() instanceof ItemShulkerBox) {
                    this.drawer.draw((GuiScreen)gui, itemInHand, ItemStack.field_190927_a, event.getMouseX() + 10, event.getMouseY());
                }
            }
            else if (itemInHand.func_77973_b() instanceof ItemShulkerBox) {
                this.drawer.draw((GuiScreen)gui, itemInHand, ItemStack.field_190927_a, event.getMouseX() + 10, event.getMouseY());
            }
        }
    }
    
    @SubscribeEvent
    public void onTooltipRender(final RenderTooltipEvent.PostBackground event) {
        if (null == event.getStack()) {
            return;
        }
        if (event.getStack().func_77973_b() instanceof ItemShulkerBox) {
            this.drawer.x = event.getX();
            this.drawer.y = event.getY();
        }
    }
    
    static {
        ShulkerBoxViewerEventHandler.instance = new ShulkerBoxViewerEventHandler();
    }
}
