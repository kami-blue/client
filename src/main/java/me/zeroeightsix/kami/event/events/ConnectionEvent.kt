package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent

/**
 * @author Xiaro
 *
 * Created by Xiaro on 10/09/20
 */
open class ConnectionEvent : KamiEvent() {
    class Connect() : ConnectionEvent()
    class Disconnect() : ConnectionEvent()
}