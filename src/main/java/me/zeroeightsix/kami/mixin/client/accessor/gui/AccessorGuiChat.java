package me.zeroeightsix.kami.mixin.client.accessor.gui;

import net.minecraft.client.gui.GuiChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiChat.class)
public interface AccessorGuiChat {

    @Accessor
    String getHistoryBuffer();

    @Accessor
    void setHistoryBuffer(String value);

    @Accessor
    int getSentHistoryCursor();

    @Accessor
    void setSentHistoryCursor(int value);

}
