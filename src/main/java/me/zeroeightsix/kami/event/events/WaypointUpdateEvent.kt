package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent

/**
 * @author dominikaaaa
 * @since 31/07/20 15:43
 *
 * Updated by Xiaro on 18/08/20
 */
open class WaypointUpdateEvent : KamiEvent() {
    class Get : WaypointUpdateEvent()
    class Create : WaypointUpdateEvent()
    class Remove : WaypointUpdateEvent()
}