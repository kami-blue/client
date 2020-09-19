package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent

open class WaypointUpdateEvent : KamiEvent() {
    class Get : WaypointUpdateEvent()
    class Create : WaypointUpdateEvent()
    class Remove : WaypointUpdateEvent()
    class Update : WaypointUpdateEvent()
}