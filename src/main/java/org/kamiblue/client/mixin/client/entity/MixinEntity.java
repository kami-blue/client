package org.kamiblue.client.mixin.client.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import org.kamiblue.client.module.modules.movement.SafeWalk;
import org.kamiblue.client.module.modules.movement.Velocity;
import org.kamiblue.client.module.modules.player.Freecam;
import org.kamiblue.client.module.modules.player.ViewLock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Entity.class, priority = Integer.MAX_VALUE)
public abstract class MixinEntity {

    @Shadow private int entityId;

    private boolean modifiedSneaking = false;

    @Inject(method = "applyEntityCollision", at = @At("HEAD"), cancellable = true)
    public void applyEntityCollisionHead(Entity entityIn, CallbackInfo ci) {
        Velocity.handleApplyEntityCollision((Entity) (Object) this, entityIn, ci);
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSneaking()Z", ordinal = 0, shift = At.Shift.BEFORE))
    public void moveInvokeIsSneakingPre(MoverType type, double x, double y, double z, CallbackInfo ci) {
        if (SafeWalk.shouldSafewalk(this.entityId)) {
            modifiedSneaking = true;
            SafeWalk.setSneaking(true);
        }
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSneaking()Z", ordinal = 0, shift = At.Shift.AFTER))
    public void moveInvokeIsSneakingPost(MoverType type, double x, double y, double z, CallbackInfo ci) {
        if (modifiedSneaking) {
            modifiedSneaking = false;
            SafeWalk.setSneaking(false);
        }
    }

    // Makes the camera guy instead of original player turn around when we move mouse
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    public void turn(float yaw, float pitch, CallbackInfo ci) {
        Entity casted = (Entity) (Object) this;

        if (Freecam.handleTurn(casted, yaw, pitch, ci)) return;
        ViewLock.handleTurn(casted, yaw, pitch, ci);
    }
}
