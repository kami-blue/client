package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.BlockBreakEvent;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.render.SelectionHighlight;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by 086 on 11/04/2018.
 */
@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {

//    @Shadow
//    Minecraft mc;
//    @Shadow
//    public ChunkRenderContainer renderContainer;

//    @Inject(method = "renderBlockLayer(Lnet/minecraft/util/BlockRenderLayer;)V", at = @At("HEAD"), cancellable = true)
//    public void renderBlockLayer(BlockRenderLayer blockLayerIn, CallbackInfo callbackInfo) {
//        callbackInfo.cancel();

//        this.mc.entityRenderer.enableLightmap();
//
//        if (OpenGlHelper.useVbo())
//        {
//            GlStateManager.glEnableClientState(32884);
//            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
//            GlStateManager.glEnableClientState(32888);
//            OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
//            GlStateManager.glEnableClientState(32888);
//            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
//            GlStateManager.glEnableClientState(32886);
//        }
//
//        this.renderContainer.renderChunkLayer(blockLayerIn);
//
//        if (OpenGlHelper.useVbo())
//        {
//            for (VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements())
//            {
//                VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
//                int k1 = vertexformatelement.getIndex();
//
//                switch (vertexformatelement$enumusage)
//                {
//                    case POSITION:
//                        GlStateManager.glDisableClientState(32884);
//                        break;
//                    case UV:
//                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + k1);
//                        GlStateManager.glDisableClientState(32888);
//                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
//                        break;
//                    case COLOR:
//                        GlStateManager.glDisableClientState(32886);
//                        GlStateManager.resetColor();
//                }
//            }
//        }
//
//        this.mc.entityRenderer.disableLightmap();
//    }

    @Inject(method = "drawSelectionBox", at = @At("HEAD"), cancellable = true)
    public void drawSelectionBox(EntityPlayer player, RayTraceResult movingObjectPositionIn, int execute, float partialTicks, CallbackInfo ci) {
        SelectionHighlight sh = ModuleManager.getModuleT(SelectionHighlight.class);
        if (sh.isEnabled() && sh.getBlock().getValue()) {
            ci.cancel();
        }
    }

    @Inject(method = "sendBlockBreakProgress", at = @At("HEAD"))
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress, CallbackInfo ci) {
        BlockBreakEvent event = new BlockBreakEvent(breakerId, pos, progress);
        KamiMod.EVENT_BUS.post(event);
    }
}
