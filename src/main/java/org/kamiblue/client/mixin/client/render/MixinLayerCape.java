package org.kamiblue.client.mixin.client.render;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import org.kamiblue.client.module.modules.client.Capes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerCape.class)
public class MixinLayerCape {
    @Shadow @Final private RenderPlayer playerRenderer;

    @Inject(method = "doRenderLayer", at = @At("HEAD"), cancellable = true)
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci) {
        if (Capes.INSTANCE.tryRenderCape(playerRenderer, player, partialTicks))
            ci.cancel();
    }
}
