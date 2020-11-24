package me.zeroeightsix.kami.mixin.client.render;

import me.zeroeightsix.kami.module.modules.render.NoRender;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Idea from littlebroto1
 */
@Mixin(targets = "net.minecraft.client.gui.MapItemRenderer$Instance")
public class MixinMapItemRenderer {

    private final ResourceLocation kamiMap = new ResourceLocation("kamiblue/kamimap.png");

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;enableAlpha()V"))
    public void render(boolean noOverlayRendering, CallbackInfo ci) {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.getMap().getValue()) Wrapper.getMinecraft().getTextureManager().bindTexture(kamiMap);
    }

}
