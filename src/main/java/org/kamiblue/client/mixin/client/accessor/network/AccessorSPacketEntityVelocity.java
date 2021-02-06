package org.kamiblue.client.mixin.client.accessor.network;

import net.minecraft.network.play.server.SPacketEntityVelocity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketEntityVelocity.class)
public interface AccessorSPacketEntityVelocity {

    @Accessor("motionX")
    void setMotionX(int value);

    @Accessor("motionY")
    void setMotionY(int value);

    @Accessor("motionZ")
    void setMotionZ(int value);

}
