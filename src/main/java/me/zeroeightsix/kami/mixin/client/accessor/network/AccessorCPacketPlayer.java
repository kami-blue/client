package me.zeroeightsix.kami.mixin.client.accessor.network;

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

    @Accessor
    void setYaw(float value);

    @Accessor
    void setPitch(float value);

    @Accessor
    void setOnGround(boolean value);

    @Accessor
    boolean getMoving();

    @Accessor
    boolean getRotating();

}
