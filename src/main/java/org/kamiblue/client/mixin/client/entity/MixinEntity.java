package org.kamiblue.client.mixin.client.entity;

import org.kamiblue.client.event.KamiEventBus;
import org.kamiblue.client.event.events.EntityCollisionEvent;
import org.kamiblue.client.module.modules.movement.SafeWalk;
import org.kamiblue.client.module.modules.player.Freecam;
import org.kamiblue.client.module.modules.player.Scaffold;
import org.kamiblue.client.util.Wrapper;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Entity.class, priority = Integer.MAX_VALUE)
public class MixinEntity {

    @Shadow private int entityId;

    @Redirect(method = "applyEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    public void addVelocity(Entity entity, double x, double y, double z) {
        EntityCollisionEvent event = new EntityCollisionEvent(entity, x, y, z);
        KamiEventBus.INSTANCE.post(event);
        if (event.getCancelled()) return;

        entity.motionX += x;
        entity.motionY += y;
        entity.motionZ += z;

        entity.isAirBorne = true;
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSneaking()Z", ordinal = 0))
    public boolean isSneaking(Entity entity) {
        return SafeWalk.INSTANCE.shouldSafewalk()
            || (Scaffold.INSTANCE.isEnabled() && Scaffold.INSTANCE.getSafeWalk())
            || entity.isSneaking();
    }

    // Makes the camera guy instead of original player turn around when we move mouse
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    public void turn(float yaw, float pitch, CallbackInfo ci) {
        if (Wrapper.getPlayer() != null && this.entityId != Wrapper.getPlayer().getEntityId()) return;
        if (Freecam.INSTANCE.isEnabled() && Freecam.INSTANCE.getCameraGuy() != null) {
            Freecam.INSTANCE.getCameraGuy().turn(yaw, pitch);
            ci.cancel();
        }
    }
}
