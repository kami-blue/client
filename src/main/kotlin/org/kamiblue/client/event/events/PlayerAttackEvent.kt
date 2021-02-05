package org.kamiblue.client.event.events

import org.kamiblue.client.event.Cancellable
import org.kamiblue.client.event.Event
import net.minecraft.entity.Entity

class PlayerAttackEvent(val entity: Entity) : Event, Cancellable()