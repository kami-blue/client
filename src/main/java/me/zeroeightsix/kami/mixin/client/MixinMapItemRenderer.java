package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.render.NoMaps;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapItemRenderer.class)
public class MixinMapItemRenderer {

    private ResourceLocation hiddenLocation = new ResourceLocation("kamiblue/logo128x128.png");
    private ResourceLocation realLocation;

    @Shadow
    public MapItemRenderer.Instance getMapRendererInstance(MapData mapdataIn) {
        return (MapItemRenderer.Instance) null;
    }

    @Inject(method = "renderMap", at = @At("HEAD"), cancellable = true)
    public void doRenderMap(MapData mapdataIn, boolean noOverlayRendering, CallbackInfo callbackInfo) {
        MapItemRenderer.Instance instance = this.getMapRendererInstance(mapdataIn);
        if (realLocation == null) {
            realLocation = instance.location;
        }
        if (ModuleManager.isModuleEnabled(NoMaps.class)) {
            instance.location = hiddenLocation;
        } else {
            instance.location = realLocation;
        }
        instance.render(noOverlayRendering);
        callbackInfo.cancel();
    }
}
