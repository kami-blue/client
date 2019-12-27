package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.bewwawho.gui.CleanGUI;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/***
 * @author 3arthqu4ke
 * Updated by S-B99 on 27/12/19
 */
@Mixin(GuiNewChat.class)
public abstract class MixinGuiNewChat {

    @Redirect(method = "drawChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiNewChat;drawRect(IIIII)V"))
    private void drawRectBackgroundClean(int left, int top, int right, int bottom, int color) {
        if (!CleanGUI.enabled() || (CleanGUI.enabled() && !CleanGUI.chatGlobal.getValue())) { //TODO: changing this value doesn't work
//        if (!ModuleManager.isModuleEnabled("CleanGUI")) {
            Gui.drawRect(left, top, right, bottom, color);
        }
    }

}