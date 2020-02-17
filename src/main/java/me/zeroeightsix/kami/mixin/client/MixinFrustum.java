package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.ModuleManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created by 20kdc on 14/02/2020.
 */
@Mixin(Frustum.class)
public abstract class MixinFrustum {

    @Inject(method = "Lnet/minecraft/client/renderer/culling/Frustum;isBoundingBoxInFrustum(Lnet/minecraft/util/math/AxisAlignedBB;)Z", at = @At("HEAD"), cancellable = true)
    public void isBoundingBoxEtc(AxisAlignedBB ignore, CallbackInfoReturnable<Boolean> info) {
        // [WebringOfTheDamned]
        // This is used because honestly the Mojang frustrum bounding box thing is a mess.
        // This & MixinEntityRenderer get it working on OptiFine, but MixinVisGraph is necessary on Vanilla.
        if (ModuleManager.isModuleEnabled("Freecam"))
            info.setReturnValue(true);
    }

}
