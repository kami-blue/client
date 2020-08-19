package me.zeroeightsix.kami.event.events;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingEvent;

public class LocalPlayerUpdateEvent extends LivingEvent {
    public LocalPlayerUpdateEvent(EntityLivingBase entityLivingBase) {
        super(entityLivingBase);
    }
}
