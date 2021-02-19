package org.kamiblue.client.mixin.client.render;

import net.minecraft.client.model.ModelElytra;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerElytra;
import net.minecraft.entity.EntityLivingBase;
import org.kamiblue.client.module.modules.client.Capes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerElytra.class)
public class MixinLayerElytra {

    @Shadow @Final protected RenderLivingBase<?> renderPlayer;
    @Shadow @Final private ModelElytra modelElytra;

    @Inject(method = "doRenderLayer", at = @At("HEAD"), cancellable = true)
    public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci) {
        if (Capes.INSTANCE.tryRenderElytra(renderPlayer, modelElytra, entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, partialTicks))
            ci.cancel();
    }
}
