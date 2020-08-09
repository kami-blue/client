package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.modules.movement.ElytraFlight;
import net.minecraft.client.audio.ElytraSound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

@Mixin(ElytraSound.class)
public class MixinElytraSound {
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    public void update(CallbackInfo ci) {
        ElytraFlight elytraFlight = MODULE_MANAGER.getModuleT(ElytraFlight.class);
        if (elytraFlight.isEnabled() && !elytraFlight.getElytraSounds().getValue()) {
            ci.cancel();
        }
    }
}
