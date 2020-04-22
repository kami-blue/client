package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
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

    @Inject(method = "actionPerformed", at = @At("HEAD"), cancellable = true)
    public void onActionPerformed(GuiButton btn, CallbackInfo callbackInfo) {
        if (!KamiMod.hasAskedToUpdate && KamiMod.latest != null) {
            if (!KamiMod.isLatest) {
                if (btn.id == 1) {
                    Wrapper.getMinecraft().displayGuiScreen(new KamiGuiUpdateNotification("KAMI Blue Update", "A newer release of KAMI Blue is available (" + KamiMod.latest + ").", btn.id));
                    KamiMod.hasAskedToUpdate = true;

                    callbackInfo.cancel();
                }

                if (btn.id == 2) {
                    Wrapper.getMinecraft().displayGuiScreen(new KamiGuiUpdateNotification("KAMI Blue Update", "A newer release of KAMI Blue is available (" + KamiMod.latest + ").", btn.id));
                    KamiMod.hasAskedToUpdate = true;

                    callbackInfo.cancel();
                }
            }
        }
    }
}
