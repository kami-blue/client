package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.modules.client.Capes;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Crystalinqq
 * Updated by 20kdc on 17/02/20 - changed implementation method, made a module again, made async
 */
@Mixin(AbstractClientPlayer.class)
public class MixinAbstractClientPlayer {

    @Inject(method = "getLocationCape", at = @At(value = "RETURN"), cancellable = true)
    public void getCape(CallbackInfoReturnable<ResourceLocation> callbackInfo) {
        if (Capes.INSTANCE == null)
            return;
        if ((!Capes.INSTANCE.getOverrideOtherCapes().getValue()) && (callbackInfo.getReturnValue() != null))
            return;
        // This is weird with a capital W.
        // Essentially, the "mixin class" content is actually aliased over to the actual target class.
        // But that's a runtime thing, so the Java Compiler doesn't know anything about this.
        ResourceLocation kamiCape = Capes.getCapeResource((AbstractClientPlayer) (Object) this);
        if (kamiCape != null)
            callbackInfo.setReturnValue(kamiCape);
    }
}
