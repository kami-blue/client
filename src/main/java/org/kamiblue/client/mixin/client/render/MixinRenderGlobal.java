package org.kamiblue.client.mixin.client.render;

import org.kamiblue.client.event.KamiEventBus;
import org.kamiblue.client.event.events.BlockBreakEvent;
import org.kamiblue.client.mixin.client.accessor.render.AccessorViewFrustum;
import org.kamiblue.client.module.modules.player.Freecam;
import org.kamiblue.client.module.modules.render.SelectionHighlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {

    @Shadow private int renderDistanceChunks;
    @Shadow private ViewFrustum viewFrustum;

    private final Minecraft mc = Minecraft.getMinecraft();

    @Inject(method = "drawSelectionBox", at = @At("HEAD"), cancellable = true)
    public void drawSelectionBox(EntityPlayer player, RayTraceResult movingObjectPositionIn, int execute, float partialTicks, CallbackInfo ci) {
        if (SelectionHighlight.INSTANCE.isEnabled() && SelectionHighlight.INSTANCE.getBlock().getValue()) {
            ci.cancel();
        }
    }

    @Inject(method = "sendBlockBreakProgress", at = @At("HEAD"))
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress, CallbackInfo ci) {
        BlockBreakEvent event = new BlockBreakEvent(breakerId, pos, progress);
        KamiEventBus.INSTANCE.post(event);
    }

    // Can't use @ModifyVariable here because it crashes outside of a dev env with Optifine
    @Redirect(method = "setupTerrain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;getRenderChunkOffset(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/chunk/RenderChunk;Lnet/minecraft/util/EnumFacing;)Lnet/minecraft/client/renderer/chunk/RenderChunk;"))
    public RenderChunk renderChunkOffset(RenderGlobal renderGlobal, BlockPos playerPos, RenderChunk renderChunkBase, EnumFacing facing) {
        if (Freecam.INSTANCE.isEnabled()) {
            playerPos = Freecam.getRenderChunkOffset();
        }

        // Can't use a @Shadow of getRenderChunkOffset because it crashes outside of a dev env with Optifine
        BlockPos blockpos = renderChunkBase.getBlockPosOffset16(facing);

        if (MathHelper.abs(playerPos.getX() - blockpos.getX()) > this.renderDistanceChunks * 16) {
            return null;
        } else if (blockpos.getY() >= 0 && blockpos.getY() < 256) {
            return MathHelper.abs(playerPos.getZ() - blockpos.getZ()) > this.renderDistanceChunks * 16 ? null : ((AccessorViewFrustum) this.viewFrustum).invokeGetRenderChunk(blockpos);
        } else {
            return null;
        }
    }

    /*
     * updateChunkPositions loadRenderers as well, but as long as you don't change your renderDistance in Freecam loadRenderers won't be called
     * One could add the same redirect for loadRenderers if needed
     */
    @Redirect(method = "setupTerrain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ViewFrustum;updateChunkPositions(DD)V"))
    public void updateSetupTerrain(ViewFrustum viewFrustum, double viewEntityX, double viewEntityZ) {
        if (Freecam.INSTANCE.isEnabled()) {
            viewEntityX = mc.player.posX;
            viewEntityZ = mc.player.posZ;
        }
        viewFrustum.updateChunkPositions(viewEntityX, viewEntityZ);
    }
}
