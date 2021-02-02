package org.kamiblue.client.event.events

import org.kamiblue.client.event.Event

abstract class ConnectionEvent : Event {
    class Connect : ConnectionEvent()
    class Disconnect : ConnectionEvent()
}