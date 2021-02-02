package org.kamiblue.client.event.events

import org.kamiblue.client.event.Event
import org.kamiblue.client.manager.managers.WaypointManager.Waypoint

class WaypointUpdateEvent(val type: Type, val waypoint: Waypoint?) : Event {
    enum class Type {
        GET, ADD, REMOVE, CLEAR, RELOAD
    }
}