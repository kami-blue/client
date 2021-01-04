package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.Event
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.TickEvent

/**
 * Similar to [TickEvent.ClientTickEvent], but with a null check
 *
 * @see safeListener
 * @see TickEvent.ClientTickEvent
 */
@Deprecated("Use safeListener with TickEvent.ClientTickEvent instead")
class SafeTickEvent(val phase: TickEvent.Phase) : Event