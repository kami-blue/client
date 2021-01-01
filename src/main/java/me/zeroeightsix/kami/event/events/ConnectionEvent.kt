package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.Event

abstract class ConnectionEvent : Event {
    class Connect : ConnectionEvent()
    class Disconnect : ConnectionEvent()
}