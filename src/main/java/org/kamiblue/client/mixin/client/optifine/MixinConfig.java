package org.kamiblue.client.mixin.client.optifine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Thanks optifine :wheelchair:
 */
@Pseudo
@SuppressWarnings("UnresolvedMixinReference")
@Mixin(targets = "Config", remap = false)
public class MixinConfig {
    @Inject(method = "isFastRender", at = @At("HEAD"), cancellable = true, remap = false)
    private static void isFastRender(CallbackInfoReturnable<Boolean> isFastRender) {
        isFastRender.setReturnValue(false);
    }
}
