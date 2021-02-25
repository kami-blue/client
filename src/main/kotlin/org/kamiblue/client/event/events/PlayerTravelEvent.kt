package org.kamiblue.client.event.events

import org.kamiblue.client.event.Cancellable
import org.kamiblue.client.event.Event
import org.kamiblue.client.event.ICancellable
import org.kamiblue.client.event.ProfilerEvent

class PlayerTravelEvent : Event, ICancellable by Cancellable(), ProfilerEvent {
    override val profilerName: String = "kbPlayerTravel"
}