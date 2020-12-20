package me.zeroeightsix.kami.mixin.client.accessor.player;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerControllerMP.class)
public interface AccessorPlayerControllerMP {

    @Accessor
    int getBlockHitDelay();

    @Accessor
    void setBlockHitDelay(int value);

    @Accessor
    int getCurrentPlayerItem();

    @Invoker
    void invokeSyncCurrentPlayItem();

}
