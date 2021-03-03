package org.kamiblue.client.event.events

import org.kamiblue.client.event.Event
import org.kamiblue.client.event.ProfilerEvent

sealed class RunGameLoopEvent(override val profilerName: String) : Event, ProfilerEvent {
    class Start : RunGameLoopEvent("start")
    class Tick : RunGameLoopEvent("tick")
    class Render : RunGameLoopEvent("render")
    class End : RunGameLoopEvent("end")
}