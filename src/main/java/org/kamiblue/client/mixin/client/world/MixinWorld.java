package org.kamiblue.client.mixin.client.world;

import org.kamiblue.client.module.modules.misc.AntiWeather;
import org.kamiblue.client.module.modules.render.NoRender;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = World.class, priority = Integer.MAX_VALUE)
public class MixinWorld {
    @Inject(method = "checkLightFor", at = @At("HEAD"), cancellable = true)
    private void checkLightFor(EnumSkyBlock lightType, BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
        if (lightType == EnumSkyBlock.SKY && NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.getSkylight().getValue()) {
            ci.setReturnValue(false);
        }
    }

    @Inject(method = "isRaining", at = @At("RETURN"), cancellable = true)
    private void isRaining(CallbackInfoReturnable<Boolean> cir) {
        if (AntiWeather.INSTANCE.isEnabled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isThundering", at = @At("RETURN"), cancellable = true)
    private void isThundering(CallbackInfoReturnable<Boolean> cir) {
        if (AntiWeather.INSTANCE.isEnabled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getRainStrength", at = @At("RETURN"), cancellable = true)
    private void getRainStrength(float delta, CallbackInfoReturnable<Float> cir) {
        if (AntiWeather.INSTANCE.isEnabled()) {
            cir.setReturnValue(0f);
        }
    }
}
