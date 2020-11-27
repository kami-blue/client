package me.zeroeightsix.kami.mixin.client.accessor.network;

import net.minecraft.network.play.server.SPacketPlayerPosLook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketPlayerPosLook.class)
public interface AccessorSPacketPosLook {

    @Accessor
    void setYaw(float value);

    @Accessor
    void setPitch(float value);

}
