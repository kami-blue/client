package org.kamiblue.client.mixin.client.entity;

import net.minecraft.entity.passive.EntityPig;
import org.kamiblue.client.module.modules.movement.EntitySpeed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created by 086 on 16/12/2017.
 */
@Mixin(EntityPig.class)
public class MixinEntityPig {

    @Inject(method = "canBeSteered", at = @At("HEAD"), cancellable = true)
    public void canBeSteered(CallbackInfoReturnable<Boolean> returnable) {
        if (EntitySpeed.INSTANCE.isEnabled()) {
            returnable.setReturnValue(true);
            returnable.cancel();
        }
    }

}
