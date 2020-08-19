package me.zeroeightsix.kami.event.events;

import me.zeroeightsix.kami.event.KamiEvent;
import net.minecraft.entity.Entity;

import javax.annotation.Nonnull;

public class ClientPlayerAttackEvent extends KamiEvent {

    private Entity targetEntity;

    public ClientPlayerAttackEvent(@Nonnull Entity targetEntity) {
        if (this.targetEntity == null) {
            throw new IllegalArgumentException("Target Entity cannot be null");
        }
        this.targetEntity = targetEntity;
    }

    public Entity getTargetEntity() {
        return targetEntity;
    }

}
