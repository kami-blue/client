package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

/**
 * Similar to [TickEvent.ClientTickEvent], but with a null check
 */
class SafeTickEvent(val phase: TickEvent.Phase) : KamiEvent()