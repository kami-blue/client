package org.kamiblue.client.mixin.client.player;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.kamiblue.client.event.KamiEventBus;
import org.kamiblue.client.event.events.PlayerTravelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityPlayer.class, priority = Integer.MAX_VALUE)
public abstract class MixinEntityPlayer extends EntityLivingBase {

    public MixinEntityPlayer(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void travel(float strafe, float vertical, float forward, CallbackInfo info) {
        //noinspection ConstantConditions
        if (EntityPlayerSP.class.isAssignableFrom(this.getClass())) {
            PlayerTravelEvent event = new PlayerTravelEvent();
            KamiEventBus.INSTANCE.post(event);
            if (event.getCancelled()) {
                move(MoverType.SELF, motionX, motionY, motionZ);
                info.cancel();
            }
        }
    }
}