package org.kamiblue.client.mixin.client.render;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import org.kamiblue.client.module.modules.render.Nametags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Render.class)
abstract class MixinRender<T extends Entity> {
    @Inject(method = "renderLivingLabel", at = @At("HEAD"), cancellable = true)
    protected void renderNamePre(T entityIn, String str, double x, double y, double z, int maxDistance, CallbackInfo ci) {
        if (Nametags.INSTANCE.isEnabled() && Nametags.INSTANCE.checkEntityType(entityIn)) {
            ci.cancel();
        }
    }
}
