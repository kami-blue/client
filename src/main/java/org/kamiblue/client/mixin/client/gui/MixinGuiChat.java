package org.kamiblue.client.mixin.client.gui;

import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.kamiblue.client.command.CommandManager;
import org.kamiblue.client.gui.mc.KamiGuiChat;
import org.kamiblue.client.util.Wrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiChat.class)
public abstract class MixinGuiChat extends GuiScreen {

    @Shadow protected GuiTextField inputField;
    @Shadow private String historyBuffer;
    @Shadow private int sentHistoryCursor;

    @Inject(method = "keyTyped(CI)V", at = @At("RETURN"))
    public void returnKeyTyped(char typedChar, int keyCode, CallbackInfo info) {
        GuiScreen currentScreen = Wrapper.getMinecraft().currentScreen;
        if (currentScreen instanceof GuiChat && !(currentScreen instanceof KamiGuiChat)
            && inputField.getText().startsWith(CommandManager.INSTANCE.getPrefix())) {
            Wrapper.getMinecraft().displayGuiScreen(new KamiGuiChat(inputField.getText(), historyBuffer, sentHistoryCursor));
        }
    }

}
