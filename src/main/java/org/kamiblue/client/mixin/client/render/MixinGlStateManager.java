package org.kamiblue.client.mixin.client.render;

import net.minecraft.client.renderer.GlStateManager;
import org.kamiblue.client.util.graphics.GlStateUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class MixinGlStateManager {

    @Inject(method = "color(FFF)V", at = @At("HEAD"), cancellable = true)
    private static void color3f(float colorRed, float colorGreen, float colorBlue, CallbackInfo ci) {
        if (GlStateUtils.getColorLock()) ci.cancel();
    }

    @Inject(method = "color(FFFF)V", at = @At("HEAD"), cancellable = true)
    private static void color4f(float colorRed, float colorGreen, float colorBlue, float colorAlpha, CallbackInfo ci) {
        if (GlStateUtils.getColorLock()) ci.cancel();
    }

    @Inject(method = "loadIdentity", at = @At("HEAD"))
    private static void loadIdentity(CallbackInfo ci) {
        GlStateUtils.colorLock(false);
    }

}
