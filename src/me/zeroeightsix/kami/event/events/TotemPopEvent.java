// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.event.events;

import net.minecraft.entity.Entity;
import me.zeroeightsix.kami.event.KamiEvent;

public class TotemPopEvent extends KamiEvent
{
    private Entity entity;
    
    public TotemPopEvent(final Entity entity) {
        this.entity = entity;
    }
    
    public Entity getEntity() {
        return this.entity;
    }
}
