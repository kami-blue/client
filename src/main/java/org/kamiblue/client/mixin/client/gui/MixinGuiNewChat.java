package org.kamiblue.client.mixin.client.gui;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.text.ITextComponent;
import org.kamiblue.client.module.modules.chat.ExtraChatHistory;
import org.kamiblue.client.module.modules.render.CleanGUI;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiNewChat.class)
public abstract class MixinGuiNewChat {
    @Shadow @Final private List<ChatLine> chatLines;
    @Shadow @Final private List<ChatLine> drawnChatLines;

    @Redirect(method = "drawChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiNewChat;drawRect(IIIII)V"))
    private void drawRectBackgroundClean(int left, int top, int right, int bottom, int color) {
        if (!CleanGUI.INSTANCE.isEnabled() || !CleanGUI.INSTANCE.getChatGlobal().getValue()) {
            Gui.drawRect(left, top, right, bottom, color);
        }
    }

    @Inject(method = "setChatLine", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 0, remap = false), cancellable = true)
    public void setChatLineInvokeSize(ITextComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly, CallbackInfo ci) {
        ExtraChatHistory.handleSetChatLine(drawnChatLines, chatLines, chatComponent, chatLineId, updateCounter, displayOnly, ci);
    }
}