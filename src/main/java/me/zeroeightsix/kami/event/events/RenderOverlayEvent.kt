package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.Event
import me.zeroeightsix.kami.event.ProfilerEvent

class RenderOverlayEvent : Event, ProfilerEvent {
    override val profilerName: String = "kbRender2D"
}