// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.mixin.client;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.client.gui.GuiScreen;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.gui.mc.KamiGuiChat;
import me.zeroeightsix.kami.util.Wrapper;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiChat;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({ GuiChat.class })
public abstract class MixinGuiChat
{
    @Shadow
    protected GuiTextField field_146415_a;
    @Shadow
    public String field_146410_g;
    @Shadow
    public int field_146416_h;
    
    @Shadow
    public abstract void func_73866_w_();
    
    @Inject(method = { "Lnet/minecraft/client/gui/GuiChat;keyTyped(CI)V" }, at = { @At("RETURN") })
    public void returnKeyTyped(final char typedChar, final int keyCode, final CallbackInfo info) {
        if (!(Wrapper.getMinecraft().field_71462_r instanceof GuiChat) || Wrapper.getMinecraft().field_71462_r instanceof KamiGuiChat) {
            return;
        }
        if (this.field_146415_a.func_146179_b().startsWith(Command.getCommandPrefix())) {
            Wrapper.getMinecraft().func_147108_a((GuiScreen)new KamiGuiChat(this.field_146415_a.func_146179_b(), this.field_146410_g, this.field_146416_h));
        }
    }
}
