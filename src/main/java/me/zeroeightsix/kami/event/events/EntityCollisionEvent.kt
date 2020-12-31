package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.Cancellable
import me.zeroeightsix.kami.event.ICancellable
import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.entity.Entity

open class EntityCollisionEvent(
    val entity: Entity,
    var x: Double,
    var y: Double,
    var z: Double
) : KamiEvent(), ICancellable by Cancellable()