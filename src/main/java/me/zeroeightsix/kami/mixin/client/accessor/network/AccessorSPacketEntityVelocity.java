package me.zeroeightsix.kami.mixin.client.accessor.network;

import net.minecraft.network.play.server.SPacketEntityVelocity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketEntityVelocity.class)
public interface AccessorSPacketEntityVelocity {

    @Accessor
    void setMotionX(int value);

    @Accessor
    void setMotionY(int value);

    @Accessor
    void setMotionZ(int value);

}
