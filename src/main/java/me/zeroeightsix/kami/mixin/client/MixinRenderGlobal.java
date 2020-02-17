package me.zeroeightsix.kami.mixin.client;

import java.util.Deque;
import me.zeroeightsix.kami.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkRenderContainer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created by 086 on 11/04/2018.
 */
@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {

    @Shadow
    Minecraft mc;
    @Shadow
    public ChunkRenderContainer renderContainer;

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

}
