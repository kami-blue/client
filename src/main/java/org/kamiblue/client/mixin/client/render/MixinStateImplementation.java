package org.kamiblue.client.mixin.client.render;

import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.kamiblue.client.module.modules.render.Xray;

@Mixin(BlockStateContainer.StateImplementation.class)
public class MixinStateImplementation {

    @Inject(method = "shouldSideBeRendered", at = @At("HEAD"), cancellable = true)
    public void shouldSideBeRendered(IBlockAccess blockAccess, BlockPos pos, EnumFacing facing, CallbackInfoReturnable<Boolean> cir) {
        if (Xray.INSTANCE.isEnabled()) {
            if (Xray.INSTANCE.shouldReplace(blockAccess.getBlockState(pos.offset(facing)))) {
                cir.setReturnValue(true);
            }
        }
    }
}
