package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.modules.movement.NoSlowDown;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

/**
 * @see MixinBlockSoulSand
 * @author 086
 */
@Mixin(BlockWeb.class)
public class MixinBlockWeb {

    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn, CallbackInfo info) {
        // If noslowdown is on, just don't do anything else in this method (slow the player)
        if (MODULE_MANAGER.isModuleEnabled(NoSlowDown.class) && ((NoSlowDown) MODULE_MANAGER.getModule(NoSlowDown.class)).cobweb.getValue()) info.cancel();
    }

}
