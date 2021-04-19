package org.kamiblue.client.mixin.client.render;

import net.minecraft.entity.Entity;
import org.kamiblue.client.module.modules.render.Xray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinEntity {

    @Inject(method = "getBrightnessForRender", at = @At("HEAD"), cancellable = true)
    public void getBrightnessForRender(CallbackInfoReturnable<Integer> cir) {
        if (Xray.INSTANCE.isEnabled()) {
            cir.setReturnValue(15);
        }
    }
}
