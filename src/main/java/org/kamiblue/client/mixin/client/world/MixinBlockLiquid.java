package org.kamiblue.client.mixin.client.world;

import org.kamiblue.client.module.modules.movement.Velocity;
import org.kamiblue.client.module.modules.player.LiquidInteract;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockLiquid.class)
public class MixinBlockLiquid {

    @Inject(method = "modifyAcceleration", at = @At("HEAD"), cancellable = true)
    public void modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3d motion, CallbackInfoReturnable<Vec3d> returnable) {
        if (Velocity.INSTANCE.isEnabled()) {
            returnable.setReturnValue(motion);
            returnable.cancel();
        }
    }

    /**
     * Taken from Minecraft code {@link BlockLiquid#canCollideCheck}
     */
    @Inject(method = "canCollideCheck", at = @At("HEAD"), cancellable = true)
    public void canCollideCheck(final IBlockState blockState, final boolean hitIfLiquid, final CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        callbackInfoReturnable.setReturnValue((hitIfLiquid && blockState.getValue(BlockLiquid.LEVEL) == 0) || LiquidInteract.INSTANCE.isEnabled());
    }
}
