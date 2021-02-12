package org.kamiblue.client.mixin.client;

import net.minecraft.client.audio.ElytraSound;
import org.kamiblue.client.module.modules.movement.ElytraFlight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ElytraSound.class)
public class MixinElytraSound {
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    public void update(CallbackInfo ci) {
        if (ElytraFlight.INSTANCE.isEnabled() && !ElytraFlight.INSTANCE.getElytraSounds()) {
            ci.cancel();
        }
    }
}
