package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.Cancellable
import me.zeroeightsix.kami.event.Event
import me.zeroeightsix.kami.event.ICancellable
import net.minecraft.entity.Entity

class ClientPlayerAttackEvent(val entity: Entity) : Event(), ICancellable by Cancellable()