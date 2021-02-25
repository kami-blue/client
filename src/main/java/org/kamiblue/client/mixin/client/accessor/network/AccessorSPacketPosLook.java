package org.kamiblue.client.mixin.client.accessor.network;

import net.minecraft.network.play.server.SPacketPlayerPosLook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketPlayerPosLook.class)
public interface AccessorSPacketPosLook {

    @Accessor("yaw")
    void setYaw(float value);

    @Accessor("pitch")
    void setPitch(float value);

}
