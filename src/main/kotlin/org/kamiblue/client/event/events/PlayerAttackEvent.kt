package org.kamiblue.client.event.events

import net.minecraft.entity.Entity
import org.kamiblue.client.event.Cancellable
import org.kamiblue.client.event.Event

class PlayerAttackEvent(val entity: Entity) : Event, Cancellable()