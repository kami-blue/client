package org.kamiblue.client.mixin.client.render;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import org.kamiblue.client.event.KamiEventBus;
import org.kamiblue.client.event.events.RenderOverlayEvent;
import org.kamiblue.client.module.modules.movement.ElytraFlight;
import org.kamiblue.client.module.modules.player.BlockInteraction;
import org.kamiblue.client.module.modules.render.AntiFog;
import org.kamiblue.client.module.modules.render.AntiOverlay;
import org.kamiblue.client.module.modules.render.CameraClip;
import org.kamiblue.client.util.Wrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityRenderer.class, priority = Integer.MAX_VALUE)
public class MixinEntityRenderer {

    @Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;renderGameOverlay(F)V", shift = At.Shift.AFTER))
    public void updateCameraAndRender(float partialTicks, long nanoTime, CallbackInfo ci) {
        KamiEventBus.INSTANCE.post(new RenderOverlayEvent());
    }

    @ModifyVariable(method = "orientCamera", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    public RayTraceResult orientCameraStoreRayTraceBlocks(RayTraceResult value) {
        if (CameraClip.INSTANCE.isEnabled()) {
            return null;
        } else {
            return value;
        }
    }

    @Inject(method = "displayItemActivation", at = @At(value = "HEAD"), cancellable = true)
    public void displayItemActivation(ItemStack stack, CallbackInfo ci) {
        if (AntiOverlay.INSTANCE.isEnabled() && AntiOverlay.INSTANCE.getTotems().getValue()) {
            ci.cancel();
        }
    }

    @Inject(method = "setupFog", at = @At(value = "RETURN"), cancellable = true)
    public void setupFog(int startCoords, float partialTicks, CallbackInfo callbackInfo) {
        if (AntiFog.INSTANCE.isEnabled()) {
            GlStateManager.disableFog();
        }
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    public void hurtCameraEffect(float ticks, CallbackInfo ci) {
        if (AntiOverlay.INSTANCE.isEnabled() && AntiOverlay.INSTANCE.getHurtCamera().getValue()) {
            ci.cancel();
        }
    }

    @Inject(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getPositionEyes(F)Lnet/minecraft/util/math/Vec3d;", shift = At.Shift.BEFORE), cancellable = true)
    public void getEntitiesInAABBexcluding(float partialTicks, CallbackInfo ci) {
        if (BlockInteraction.isNoEntityTraceEnabled()) {
            ci.cancel();
            Wrapper.getMinecraft().profiler.endSection();
        }
    }

    @Inject(method = "orientCamera", at = @At("RETURN"))
    private void orientCameraStoreEyeHeight(float partialTicks, CallbackInfo ci) {
        if (!ElytraFlight.INSTANCE.shouldSwing()) return;
        Entity entity = Wrapper.getMinecraft().getRenderViewEntity();
        if (entity != null) {
            GlStateManager.translate(0.0f, entity.getEyeHeight() - 0.4f, 0.0f);
        }
    }
}
