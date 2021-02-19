package org.kamiblue.client.mixin.client.render;

import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.world.storage.MapData;
import org.kamiblue.client.module.modules.render.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapItemRenderer.class)
public class MixinMapItemRenderer {
    @Inject(method = "renderMap", at = @At(value = "HEAD"), cancellable = true)
    public void renderMap(MapData mapdataIn, boolean noOverlayRendering, CallbackInfo ci) {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.getMap().getValue()) {
            ci.cancel();
            NoRender.INSTANCE.renderFakeMap();
        }
    }
}
