package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent

abstract class ConnectionEvent : KamiEvent() {
    class Connect : ConnectionEvent()
    class Disconnect : ConnectionEvent()
}