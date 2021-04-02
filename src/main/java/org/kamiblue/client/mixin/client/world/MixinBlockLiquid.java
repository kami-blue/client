package org.kamiblue.client.mixin.client.world;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.kamiblue.client.module.modules.movement.Velocity;
import org.kamiblue.client.module.modules.player.BlockInteraction;
import org.kamiblue.client.util.Wrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockLiquid.class)
public class MixinBlockLiquid {
    @Inject(method = "modifyAcceleration", at = @At("HEAD"), cancellable = true)
    public void modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3d motion, CallbackInfoReturnable<Vec3d> cir) {
        if (Velocity.getCancelLiquidVelocity()) {
            cir.setReturnValue(motion);
        }
    }

    @Inject(method = "canCollideCheck", at = @At("HEAD"), cancellable = true)
    public void canCollideCheck(IBlockState blockState, boolean hitIfLiquid, CallbackInfoReturnable<Boolean> cir) {
        if (BlockInteraction.isLiquidInteractEnabled() && Wrapper.getMinecraft().player.getHeldItemMainhand().getItem() instanceof ItemBlock) {
            cir.setReturnValue(true);
        }
    }
}
