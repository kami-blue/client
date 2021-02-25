package org.kamiblue.client.mixin.client.accessor.gui;

import net.minecraft.client.gui.GuiChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiChat.class)
public interface AccessorGuiChat {

    @Accessor("historyBuffer")
    String getHistoryBuffer();

    @Accessor("historyBuffer")
    void setHistoryBuffer(String value);

    @Accessor("sentHistoryCursor")
    int getSentHistoryCursor();

    @Accessor("sentHistoryCursor")
    void setSentHistoryCursor(int value);

}
