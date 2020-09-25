package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.entity.Entity

/**
 * Created by 086 on 13/11/2017.
 */
open class RenderEntityEvent(
        val entity: Entity?,
        val x: Double,
        val y: Double,
        val z: Double,
        val yaw: Float,
        override val partialTicks: Float,
        val debug: Boolean
) : KamiEvent() {

    class Pre(entity: Entity?, x: Double, y: Double, z: Double, yaw: Float, partialTicks: Float, debug: Boolean) : RenderEntityEvent(entity, x, y, z, yaw, partialTicks, debug)
    class Post(entity: Entity?, x: Double, y: Double, z: Double, yaw: Float, partialTicks: Float, debug: Boolean) : RenderEntityEvent(entity, x, y, z, yaw, partialTicks, debug)
    class Final(entity: Entity?, x: Double, y: Double, z: Double, yaw: Float, partialTicks: Float, debug: Boolean) : RenderEntityEvent(entity, x, y, z, yaw, partialTicks, debug)
}