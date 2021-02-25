package org.kamiblue.client.event.events

import org.kamiblue.client.event.Event
import org.kamiblue.client.event.KamiEventBus
import org.kamiblue.client.event.SingletonEvent

object ShutdownEvent : Event, SingletonEvent(KamiEventBus)