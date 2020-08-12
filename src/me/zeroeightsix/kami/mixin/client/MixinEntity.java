// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.modules.movement.SafeWalk;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.EntityEvent;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({ Entity.class })
public class MixinEntity
{
    @Redirect(method = { "applyEntityCollision" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    public void addVelocity(final Entity entity, final double x, final double y, final double z) {
        final EntityEvent.EntityCollision entityCollisionEvent = new EntityEvent.EntityCollision(entity, x, y, z);
        KamiMod.EVENT_BUS.post(entityCollisionEvent);
        if (entityCollisionEvent.isCancelled()) {
            return;
        }
        entity.field_70159_w += x;
        entity.field_70181_x += y;
        entity.field_70179_y += z;
        entity.field_70160_al = true;
    }
    
    @Redirect(method = { "move" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSneaking()Z"))
    public boolean isSneaking(final Entity entity) {
        return SafeWalk.shouldSafewalk() || entity.func_70093_af();
    }
}
