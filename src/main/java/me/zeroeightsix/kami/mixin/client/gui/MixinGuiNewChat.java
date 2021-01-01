package me.zeroeightsix.kami.mixin.client.gui;

import me.zeroeightsix.kami.event.KamiEventBus;
import me.zeroeightsix.kami.event.events.PrintChatMessageEvent;
import me.zeroeightsix.kami.module.modules.chat.ExtraChatHistory;
import me.zeroeightsix.kami.module.modules.render.CleanGUI;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiNewChat.class)
public abstract class MixinGuiNewChat {

    @Redirect(method = "drawChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiNewChat;drawRect(IIIII)V"))
    private void drawRectBackgroundClean(int left, int top, int right, int bottom, int color) {
        if (!CleanGUI.INSTANCE.isEnabled() || !CleanGUI.INSTANCE.getChatGlobal().getValue()) {
            Gui.drawRect(left, top, right, bottom, color);
        }
    }

    @Inject(method = "printChatMessage", at = @At("HEAD"))
    private void printChatMessage(ITextComponent chatComponent, CallbackInfo ci) {
        KamiEventBus.INSTANCE.post(new PrintChatMessageEvent(chatComponent));
    }

    @Redirect(method = "setChatLine", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 0, remap = false))
    public <E> int drawnChatLinesSize(List<E> list) {
        return getModifiedSize(list);
    }

    @Redirect(method = "setChatLine", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 2, remap = false))
    public <E> int chatLinesSize(List<E> list) {
        return getModifiedSize(list);
    }

    public <E> int getModifiedSize(List<E> list) {
        if (ExtraChatHistory.INSTANCE.isEnabled()) {
            return list.size() - ExtraChatHistory.INSTANCE.getMaxMessages().getValue() - 100;
        } else {
            return list.size();
        }
    }
}