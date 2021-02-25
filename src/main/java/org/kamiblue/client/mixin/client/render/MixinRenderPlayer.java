package org.kamiblue.client.mixin.client.render;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.kamiblue.client.module.modules.movement.ElytraFlight;
import org.kamiblue.client.util.Wrapper;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by 086 on 19/12/2017.
 */
@Mixin(RenderPlayer.class)
public class MixinRenderPlayer {

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

    // Redirect it to the original player so the original player can be renderer correctly
    @Redirect(method = "doRender", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/RenderManager;renderViewEntity:Lnet/minecraft/entity/Entity;", opcode = Opcodes.GETFIELD))
    public Entity getRenderViewEntity(RenderManager renderManager) {
        return Wrapper.getPlayer();
    }
}
