package me.zeroeightsix.kami.mixin.client.render;

import me.zeroeightsix.kami.module.modules.player.Freecam;
import me.zeroeightsix.kami.util.Wrapper;
import me.zeroeightsix.kami.util.graphics.KamiTessellator;
import me.zeroeightsix.kami.util.math.VectorUtils;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumSet;
import java.util.Set;

@Mixin(VisGraph.class)
public class MixinVisGraph {

    @Inject(method = "getVisibleFacings", at = @At("HEAD"), cancellable = true)
    public void getVisibleFacings(CallbackInfoReturnable<Set<EnumFacing>> ci) {
        // WebringOfTheDamned
        // This part prevents the "block-level culling". OptiFine does this for you but vanilla doesn't.
        // We have to implement this here or else OptiFine causes trouble.
        if (Freecam.INSTANCE.isDisabled()) return;

        WorldClient world = Wrapper.getWorld();
        if (world == null) return;

        // Only do the hacky cave culling fix if inside of a block
        Vec3d camPos = KamiTessellator.INSTANCE.getCamPos();
        BlockPos blockPos = VectorUtils.INSTANCE.toBlockPos(camPos);
        if (world.getBlockState(blockPos).isFullBlock()) {
            ci.setReturnValue(EnumSet.allOf(EnumFacing.class));
        }
    }

}
