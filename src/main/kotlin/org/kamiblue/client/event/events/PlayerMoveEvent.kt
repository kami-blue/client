package org.kamiblue.client.event.events

import net.minecraft.client.entity.EntityPlayerSP
import org.kamiblue.client.event.Cancellable
import org.kamiblue.client.event.Event

class PlayerMoveEvent(
    private val player: EntityPlayerSP
) : Event, Cancellable() {
    private val prevX = player.motionX
    private val prevY = player.motionY
    private val prevZ = player.motionZ

    val isModified: Boolean
        get() = player.motionX != prevX
            || player.motionY != prevY
            || player.motionZ != prevZ

    var x: Double
        get() = if (cancelled) 0.0 else player.motionX
        set(value) {
            if (!cancelled) player.motionX = value
        }

    var y: Double
        get() = if (cancelled) 0.0 else player.motionY
        set(value) {
            if (!cancelled) player.motionY = value
        }

    var z: Double
        get() = if (cancelled) 0.0 else player.motionZ
        set(value) {
            if (!cancelled) player.motionZ = value
        }
}