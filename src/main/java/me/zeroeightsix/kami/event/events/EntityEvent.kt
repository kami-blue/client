package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.entity.Entity

/**
 * Created by 086 on 16/11/2017.
 * Updated by Xiaro on 18/08/20
 */
open class EntityEvent(val entity: Entity) : KamiEvent() {

    class EntityCollision(entity: Entity, var x: Double, var y: Double, var z: Double) : EntityEvent(entity)
}