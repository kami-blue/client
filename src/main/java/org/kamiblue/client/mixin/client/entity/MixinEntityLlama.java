package org.kamiblue.client.mixin.client.entity;

import net.minecraft.entity.passive.EntityLlama;
import org.kamiblue.client.module.modules.movement.EntitySpeed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLlama.class)
public class MixinEntityLlama {

    @Inject(method = "canBeSteered", at = @At("RETURN"), cancellable = true)
    public void canBeSteered(CallbackInfoReturnable<Boolean> returnable) {
        if (EntitySpeed.INSTANCE.isEnabled()) returnable.setReturnValue(true);
    }

}
