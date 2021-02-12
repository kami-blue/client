package org.kamiblue.client.mixin.client.accessor.gui;

import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiDisconnected.class)
public interface AccessorGuiDisconnected {

    @Accessor("parentScreen")
    GuiScreen getParentScreen();

    @Accessor("reason")
    String getReason();

    @Accessor("message")
    ITextComponent getMessage();

}
