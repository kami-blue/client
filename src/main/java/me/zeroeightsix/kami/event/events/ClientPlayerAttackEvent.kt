package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.Cancellable
import me.zeroeightsix.kami.event.ICancellable
import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.entity.Entity

class ClientPlayerAttackEvent(val entity: Entity) : KamiEvent(), ICancellable by Cancellable()