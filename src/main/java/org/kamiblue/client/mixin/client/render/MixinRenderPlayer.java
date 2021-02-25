package org.kamiblue.client.mixin.client.render;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.util.math.Vec3d;
import org.kamiblue.client.module.modules.movement.ElytraFlight;
import org.kamiblue.client.module.modules.player.Freecam;
import org.kamiblue.client.util.Wrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer extends RenderLivingBase<AbstractClientPlayer> {

    public MixinRenderPlayer(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn, modelBaseIn, shadowSizeIn);
    }

    @Shadow protected abstract void setModelVisibilities(AbstractClientPlayer clientPlayer);

    @Inject(method = "applyRotations", at = @At("RETURN"))
    protected void applyRotations(AbstractClientPlayer entityLiving, float ageInTicks, float rotationYaw, float partialTicks, CallbackInfo ci) {
        if (entityLiving == Wrapper.getMinecraft().player && ElytraFlight.INSTANCE.shouldSwing()) {
            Vec3d vec3d = entityLiving.getLook(partialTicks);
            double d0 = entityLiving.motionX * entityLiving.motionX + entityLiving.motionZ * entityLiving.motionZ;
            double d1 = vec3d.x * vec3d.x + vec3d.z * vec3d.z;

            if (d0 > 0.0D && d1 > 0.0D) {
                double d2 = (entityLiving.motionX * vec3d.x + entityLiving.motionZ * vec3d.z) / (Math.sqrt(d0) * Math.sqrt(d1));
                double d3 = entityLiving.motionX * vec3d.z - entityLiving.motionZ * vec3d.x;
                GlStateManager.rotate(-((float) (Math.signum(d3) * Math.acos(d2)) * 180.0F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
            }
        }
    }

    // Force it to render the original player in Freecam
    @Inject(method = "doRender", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/RenderManager;renderViewEntity:Lnet/minecraft/entity/Entity;"))
    public void doRenderGetRenderViewEntity(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (Freecam.INSTANCE.isEnabled() && Wrapper.getMinecraft().getRenderViewEntity() != entity) {
            double renderY = y;

            if (entity.isSneaking()) {
                renderY = y - 0.125D;
            }

            this.setModelVisibilities(entity);
            GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
            super.doRender(entity, x, renderY, z, entityYaw, partialTicks);
            GlStateManager.disableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
        }
    }
}
