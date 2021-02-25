package org.kamiblue.client.mixin.client.accessor.network;

import net.minecraft.network.play.client.CPacketPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CPacketPlayer.class)
public interface AccessorCPacketPlayer {

    @Accessor("x")
    void setX(double value);

    @Accessor("y")
    void setY(double value);

    @Accessor("z")
    void setZ(double value);

    @Accessor("yaw")
    void setYaw(float value);

    @Accessor("pitch")
    void setPitch(float value);

    @Accessor("onGround")
    void setOnGround(boolean value);

    @Accessor("moving")
    boolean getMoving();

    @Accessor("rotating")
    boolean getRotating();

}
