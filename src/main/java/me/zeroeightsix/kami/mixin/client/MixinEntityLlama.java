package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.modules.movement.EntitySpeed;
import net.minecraft.entity.passive.EntityLlama;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

/**
 * Created by 086 on 15/10/2018.
 */
@Mixin(EntityLlama.class)
public class MixinEntityLlama {

    @Inject(method = "canBeSteered", at = @At("RETURN"), cancellable = true)
    public void canBeSteered(CallbackInfoReturnable<Boolean> returnable) {
        if (MODULE_MANAGER.isModuleEnabled(EntitySpeed.class)) returnable.setReturnValue(true);
    }

}
