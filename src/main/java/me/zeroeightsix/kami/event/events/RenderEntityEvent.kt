package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.Cancellable
import me.zeroeightsix.kami.event.ICancellable
import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.entity.Entity

abstract class RenderEntityEvent(
        val entity: Entity?,
        val x: Double,
        val y: Double,
        val z: Double,
        val yaw: Float,
        val partialTicks: Float,
        val debug: Boolean
) : KamiEvent(), ICancellable by Cancellable() {
    class Pre(entity: Entity?, x: Double, y: Double, z: Double, yaw: Float, partialTicks: Float, debug: Boolean) : RenderEntityEvent(entity, x, y, z, yaw, partialTicks, debug)
    class Post(entity: Entity?, x: Double, y: Double, z: Double, yaw: Float, partialTicks: Float, debug: Boolean) : RenderEntityEvent(entity, x, y, z, yaw, partialTicks, debug)
    class Final(entity: Entity?, x: Double, y: Double, z: Double, yaw: Float, partialTicks: Float, debug: Boolean) : RenderEntityEvent(entity, x, y, z, yaw, partialTicks, debug)
}