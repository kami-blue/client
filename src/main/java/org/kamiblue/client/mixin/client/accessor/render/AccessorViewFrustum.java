package org.kamiblue.client.mixin.client.accessor.render;

import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ViewFrustum.class)
public interface AccessorViewFrustum {

    @Invoker
    RenderChunk invokeGetRenderChunk(BlockPos pos);

}
