package org.kamiblue.client.mixin.client.accessor.network;

import net.minecraft.network.play.server.SPacketExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketExplosion.class)
public interface AccessorSPacketExplosion {

    @Accessor("motionX")
    void setMotionX(float value);

    @Accessor("motionY")
    void setMotionY(float value);

    @Accessor("motionZ")
    void setMotionZ(float value);

}
