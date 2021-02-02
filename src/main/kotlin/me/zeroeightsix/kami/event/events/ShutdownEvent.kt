package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.Event
import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.event.SingletonEvent

object ShutdownEvent : Event, SingletonEvent(KamiEventBus)