package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.movement.Velocity;
import me.zeroeightsix.kami.module.modules.player.LiquidInteract;
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

/**
 * Created by 086 on 16/12/2017.
 * Updated by dominikaaaa on 17/03/20
 */
@Mixin(BlockLiquid.class)
public class MixinBlockLiquid {

    @Inject(method = "modifyAcceleration", at = @At("HEAD"), cancellable = true)
    public void modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3d motion, CallbackInfoReturnable<Vec3d> returnable) {
        if (ModuleManager.isModuleEnabled(Velocity.class)) {
            returnable.setReturnValue(motion);
            returnable.cancel();
        }
    }

    /**
     * Taken from Minecraft code {@link BlockLiquid#canCollideCheck}
     */
    @Inject(method = "canCollideCheck", at = @At("HEAD"), cancellable = true)
    public void canCollideCheck(final IBlockState blockState, final boolean hitIfLiquid, final CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        callbackInfoReturnable.setReturnValue((hitIfLiquid && blockState.getValue(BlockLiquid.LEVEL) == 0) || ModuleManager.isModuleEnabled(LiquidInteract.class));
    }
}
