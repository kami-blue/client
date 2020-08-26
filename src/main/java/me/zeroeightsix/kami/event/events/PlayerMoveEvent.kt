package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.entity.MoverType

/**
 * @author 086
 *
 * Updated by Xiaro on 18/08/20
 */
class PlayerMoveEvent(var type: MoverType, var x: Double, var y: Double, var z: Double) : KamiEvent() 