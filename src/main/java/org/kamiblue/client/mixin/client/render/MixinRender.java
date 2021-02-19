package org.kamiblue.client.mixin.client.render;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import org.kamiblue.client.module.modules.render.ESP;
import org.kamiblue.client.module.modules.render.Nametags;
import org.kamiblue.client.util.Wrapper;
import org.kamiblue.client.util.graphics.GlStateUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

@Mixin(Render.class)
abstract class MixinRender<T extends Entity> {

    private static final FloatBuffer color = GLAllocation.createDirectFloatBuffer(16);
    private static boolean texture2d = false;
    private static boolean colorLock = false;

    @Inject(method = "renderLivingLabel", at = @At("HEAD"), cancellable = true)
    protected void renderNamePre(T entityIn, String str, double x, double y, double z, int maxDistance, CallbackInfo ci) {
        if (Nametags.INSTANCE.isEnabled() && Nametags.INSTANCE.checkEntityType(entityIn)) {
            ci.cancel();
        } else if (ESP.INSTANCE.isEnabled() && ESP.INSTANCE.getDrawingOutline()) {
            if (ESP.INSTANCE.getDrawNametag()) {
                Wrapper.getMinecraft().getFramebuffer().bindFramebuffer(false);
            } else {
                ci.cancel();
            }
        }

        if (!ci.isCancelled()) {
            colorLock = GlStateUtils.getColorLock();
            texture2d = glGetBoolean(GL_TEXTURE_2D);
            if (colorLock) {
                glGetFloat(GL_CURRENT_COLOR, color);
                GlStateUtils.colorLock(false);
            }
            if (!texture2d) {
                GlStateManager.enableTexture2D();
            }
        }
    }

    @Inject(method = "renderLivingLabel", at = @At("RETURN"))
    protected void renderNamePost(T entityIn, String str, double x, double y, double z, int maxDistance, CallbackInfo ci) {
        if (ESP.INSTANCE.isEnabled() && ESP.INSTANCE.getDrawingOutline() && ESP.INSTANCE.getFrameBuffer() != null) {
            ESP.INSTANCE.getFrameBuffer().bindFramebuffer(false);
        }

        if (colorLock) {
            GlStateManager.color(color.get(0), color.get(1), color.get(2), color.get(3));
            GlStateUtils.colorLock(true);
        }
        if (!texture2d) {
            GlStateManager.disableTexture2D();
        }
    }
}
