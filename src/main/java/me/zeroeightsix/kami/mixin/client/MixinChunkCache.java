package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.modules.render.XRay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

/**
 * Created by 20kdc on 15/02/2020.
 */
@Mixin(ChunkCache.class)
public class MixinChunkCache {

    @Inject(method = "getBlockState", at = @At("RETURN"), cancellable = true)
    public void getState(BlockPos pos, CallbackInfoReturnable<IBlockState> info) {
        if (MODULE_MANAGER.isModuleEnabled(XRay.class))
            info.setReturnValue(XRay.transform(info.getReturnValue()));
    }

}
