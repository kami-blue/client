package org.kamiblue.client.mixin.client.world;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.kamiblue.client.module.modules.render.Xray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class MixinBlock {
    @Inject(method = "getLightValue(Lnet/minecraft/block/state/IBlockState;)I", at = @At("HEAD"), cancellable = true)
    public void getLightValue(IBlockState state, CallbackInfoReturnable<Integer> cir) {
        if (Xray.INSTANCE.isEnabled()) {
            cir.setReturnValue(15);
        }
    }
}
