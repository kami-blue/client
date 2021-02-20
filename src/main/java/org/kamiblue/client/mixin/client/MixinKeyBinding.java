package org.kamiblue.client.mixin.client;

import net.minecraft.client.settings.KeyBinding;
import org.kamiblue.client.module.modules.player.AutoEat;
import org.kamiblue.client.util.Wrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public class MixinKeyBinding {

    // Fixes AutoEat gets cancelled in GUI
    @SuppressWarnings("ConstantConditions")
    @Inject(method = "isKeyDown", at = @At("HEAD"), cancellable = true)
    public void isKeyDownHead(CallbackInfoReturnable<Boolean> cir) {
        if (AutoEat.INSTANCE.isActive() && ((Object) this) == Wrapper.getMinecraft().gameSettings.keyBindUseItem) {
            cir.setReturnValue(true);
        }
    }

}
