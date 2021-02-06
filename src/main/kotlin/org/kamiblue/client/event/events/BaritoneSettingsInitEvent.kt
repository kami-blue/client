package org.kamiblue.client.event.events

import org.kamiblue.client.event.Event
import org.kamiblue.client.event.KamiEventBus
import org.kamiblue.client.event.SingletonEvent

/**
 * Posted at the return of when Baritone's Settings are initialized.
 */
object BaritoneSettingsInitEvent : Event, SingletonEvent(KamiEventBus)