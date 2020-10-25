package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import me.zeroeightsix.kami.manager.managers.WaypointManager.Waypoint

class WaypointUpdateEvent(val type: Type, val waypoint: Waypoint?) : KamiEvent() {
    enum class Type {
        GET, ADD, REMOVE, CLEAR, RELOAD
    }
}