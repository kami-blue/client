package org.kamiblue.client.event.events

import org.kamiblue.client.event.Event
import org.kamiblue.client.event.ProfilerEvent

class RenderOverlayEvent : Event, ProfilerEvent {
    override val profilerName: String = "kbRender2D"
}