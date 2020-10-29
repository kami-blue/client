package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.modules.render.NoRender;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.world.storage.MapData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapItemRenderer.class)
public class MixinMapItemRenderer {

    @Inject(method = "renderMap", at = @At("HEAD"), cancellable = true)
    public void renderMap(MapData mapdataIn, boolean noOverlayRendering, CallbackInfo ci) {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.getMap().getValue()){
            ci.cancel();
        }
    }
}
