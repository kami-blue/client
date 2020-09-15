package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.gui.mc.KamiGuiAntiDisconnect;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.misc.AntiDisconnect;
import me.zeroeightsix.kami.module.modules.movement.AutoWalk;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngameMenu.class)
public class MixinGuiIngameMenu {

    @Inject(method = "actionPerformed", at = @At("HEAD"), cancellable = true)
    public void actionPerformed(GuiButton button, CallbackInfo callbackInfo) {
        if (button.id == 1) {
            if (ModuleManager.isModuleEnabled(AntiDisconnect.class)) {
                Wrapper.getMinecraft().displayGuiScreen(new KamiGuiAntiDisconnect());

                callbackInfo.cancel();
            }

            if (!ModuleManager.isModuleEnabled(AntiDisconnect.class) && ModuleManager.isModuleEnabled(AutoWalk.class) && ModuleManager.getModuleT(AutoWalk.class).mode.getValue().equals(AutoWalk.AutoWalkMode.BARITONE)) {
                ModuleManager.getModuleT(AutoWalk.class).disable();
            }
        }
    }
}
