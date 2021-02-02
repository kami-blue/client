package org.kamiblue.client.mixin.client.render;

import org.kamiblue.client.module.modules.render.NoRender;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class MixinParticleManager {

    @Inject(method = "renderParticles", at = @At("HEAD"), cancellable = true)
    public void renderEntityPre(Entity entityIn, float partialTicks, CallbackInfo ci) {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.getParticles().getValue()) {
            ci.cancel();
        }
    }

}
