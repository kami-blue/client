package me.zeroeightsix.kami.mixin.client;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Framebuffer.class)
public class MixinFramebuffer {
    @Redirect(method = "framebufferRenderExt", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/GlStateManager.colorMask(ZZZZ)V", ordinal = 0))
    private void colorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        GlStateManager.colorMask(red, green, blue, true);
    }

    @ModifyArg(method = "createFramebuffer", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/OpenGlHelper.glRenderbufferStorage(IIII)V"), index = 1)
    private int createFrameBuffer$glRenderbufferStorage$internalFormat(int n) {
        return 34041;
    }

    @Redirect(method = "createFramebuffer", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/OpenGlHelper.glFramebufferRenderbuffer(IIII)V"))
    private void createFramebuffer$glFramebufferRenderbuffer(int target, int attachment, int renderBufferTarget, int renderBuffer) {
        OpenGlHelper.glFramebufferRenderbuffer(target, attachment, renderBufferTarget, renderBuffer);
        OpenGlHelper.glFramebufferRenderbuffer(target, 36128, renderBufferTarget, renderBuffer);
    }
}
