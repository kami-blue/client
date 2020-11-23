package me.zeroeightsix.kami.mixin.client.accessor.network;

import net.minecraft.network.play.server.SPacketExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketExplosion.class)
public interface AccessorSPacketExplosion {

    @Accessor
    void setMotionX(float value);

    @Accessor
    void setMotionY(float value);

    @Accessor
    void setMotionZ(float value);

}
