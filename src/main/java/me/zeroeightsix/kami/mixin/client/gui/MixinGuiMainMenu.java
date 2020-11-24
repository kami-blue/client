package me.zeroeightsix.kami.mixin.client.gui;

import me.zeroeightsix.kami.gui.mc.KamiGuiUpdateNotification;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by Dewy on 09/04/2020
 */
@Mixin(GuiMainMenu.class)
public abstract class MixinGuiMainMenu {

    private static boolean hasAskedToUpdate = false;

    @Inject(method = "actionPerformed", at = @At("HEAD"), cancellable = true)
    public void onActionPerformed(GuiButton button, CallbackInfo ci) {
        if (!hasAskedToUpdate && KamiGuiUpdateNotification.Companion.getLatest() != null && !KamiGuiUpdateNotification.Companion.isLatest()) {
            if (button.id == 1 || button.id == 2) {
                Wrapper.getMinecraft().displayGuiScreen(new KamiGuiUpdateNotification(button.id));
                hasAskedToUpdate = true;
                ci.cancel();
            }
        }
    }

}
