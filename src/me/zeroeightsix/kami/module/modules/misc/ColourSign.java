// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.misc;

import net.minecraft.util.text.ITextComponent;
import java.io.IOException;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.tileentity.TileEntitySign;
import java.util.function.Predicate;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiEditSign;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.GuiScreenEvent;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;

@Info(name = "ColourSign", description = "Allows ingame colouring of text on signs", category = Category.MISC)
public class ColourSign extends Module
{
    @EventHandler
    public Listener<GuiScreenEvent.Displayed> eventListener;
    
    public ColourSign() {
        this.eventListener = new Listener<GuiScreenEvent.Displayed>(event -> {
            if (event.getScreen() instanceof GuiEditSign && this.isEnabled()) {
                event.setScreen((GuiScreen)new KamiGuiEditSign(((GuiEditSign)event.getScreen()).field_146848_f));
            }
        }, (Predicate<GuiScreenEvent.Displayed>[])new Predicate[0]);
    }
    
    private class KamiGuiEditSign extends GuiEditSign
    {
        public KamiGuiEditSign(final TileEntitySign teSign) {
            super(teSign);
        }
        
        public void func_73866_w_() {
            super.func_73866_w_();
        }
        
        protected void func_146284_a(final GuiButton button) throws IOException {
            if (button.field_146127_k == 0) {
                this.field_146848_f.field_145915_a[this.field_146851_h] = (ITextComponent)new TextComponentString(this.field_146848_f.field_145915_a[this.field_146851_h].func_150254_d().replaceAll("(§)(.)", "$1$1$2$2"));
            }
            super.func_146284_a(button);
        }
        
        protected void func_73869_a(final char typedChar, final int keyCode) throws IOException {
            super.func_73869_a(typedChar, keyCode);
            String s = ((TextComponentString)this.field_146848_f.field_145915_a[this.field_146851_h]).func_150265_g();
            s = s.replace("&", "§");
            this.field_146848_f.field_145915_a[this.field_146851_h] = (ITextComponent)new TextComponentString(s);
        }
    }
}
