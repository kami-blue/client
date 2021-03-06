package org.kamiblue.client.mixin.client.render;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import org.kamiblue.client.event.KamiEventBus;
import org.kamiblue.client.event.events.BlockBreakEvent;
import org.kamiblue.client.event.events.RenderEntityEvent;
import org.kamiblue.client.module.modules.player.Freecam;
import org.kamiblue.client.module.modules.render.SelectionHighlight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {
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

    @Inject(method = "renderEntities", at = @At("HEAD"))
    public void renderEntitiesHead(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        RenderEntityEvent.setRenderingEntities(true);
    }

    @Inject(method = "renderEntities", at = @At("RETURN"))
    public void renderEntitiesReturn(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        RenderEntityEvent.setRenderingEntities(false);
    }

    @ModifyVariable(method = "setupTerrain", at = @At(value = "STORE", ordinal = 0), ordinal = 1)
    public BlockPos setupTerrainStoreFlooredChunkPosition(BlockPos playerPos) {
        if (Freecam.INSTANCE.isEnabled()) {
            playerPos = Freecam.getRenderChunkOffset(playerPos);
        }

        return playerPos;
    }
}
