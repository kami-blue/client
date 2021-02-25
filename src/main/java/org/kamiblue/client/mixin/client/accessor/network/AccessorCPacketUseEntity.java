package org.kamiblue.client.mixin.client.accessor.network;

import net.minecraft.network.play.client.CPacketUseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CPacketUseEntity.class)
public interface AccessorCPacketUseEntity {
    @Accessor("entityId")
    int getId();

    @Accessor("entityId")
    void setId(int value);

    @Accessor("action")
    void setAction(CPacketUseEntity.Action value);
}
