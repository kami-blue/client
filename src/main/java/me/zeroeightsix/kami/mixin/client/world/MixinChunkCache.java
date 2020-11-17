package me.zeroeightsix.kami.mixin.client.world;

import me.zeroeightsix.kami.module.modules.render.XRay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkCache.class)
public class MixinChunkCache {

    @Inject(method = "getBlockState", at = @At("RETURN"), cancellable = true)
    public void getState(BlockPos pos, CallbackInfoReturnable<IBlockState> info) {
        if (XRay.INSTANCE.isEnabled()) info.setReturnValue(XRay.transform(info.getReturnValue()));
    }

}
