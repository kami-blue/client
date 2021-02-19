package org.kamiblue.client.mixin.client.render;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import org.kamiblue.client.module.modules.render.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class MixinParticleManager {

    @Inject(method = "addEffect", at = @At("HEAD"), cancellable = true)
    public void addEffect(Particle effect, CallbackInfo ci) {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.handleParticle(effect)) {
            ci.cancel();
        }
    }

}
