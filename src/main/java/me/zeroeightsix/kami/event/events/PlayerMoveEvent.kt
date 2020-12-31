package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.Cancellable
import me.zeroeightsix.kami.event.Event
import me.zeroeightsix.kami.event.ICancellable
import net.minecraft.entity.MoverType

class PlayerMoveEvent(
    var type: MoverType,
    var x: Double,
    var y: Double,
    var z: Double
) : Event(), ICancellable by Cancellable()