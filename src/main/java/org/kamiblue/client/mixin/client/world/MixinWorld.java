package org.kamiblue.client.mixin.client.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.kamiblue.client.module.modules.misc.AntiWeather;
import org.kamiblue.client.module.modules.render.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = World.class, priority = Integer.MAX_VALUE)
public class MixinWorld {
    @Inject(method = "checkLightFor", at = @At("HEAD"), cancellable = true)
    private void checkLightForHead(EnumSkyBlock lightType, BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
        if (NoRender.INSTANCE.handleLighting(lightType)) {
            ci.setReturnValue(false);
        }
    }

    @Inject(method = "getThunderStrength", at = @At("HEAD"), cancellable = true)
    private void getThunderStrengthHead(float delta, CallbackInfoReturnable<Float> cir) {
        if (AntiWeather.INSTANCE.isEnabled()) {
            cir.setReturnValue(0.0f);
        }
    }

    @Inject(method = "getRainStrength", at = @At("HEAD"), cancellable = true)
    private void getRainStrengthHead(float delta, CallbackInfoReturnable<Float> cir) {
        if (AntiWeather.INSTANCE.isEnabled()) {
            cir.setReturnValue(0.0f);
        }
    }
}
