package org.kamiblue.client.manager.managers

import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.manager.Manager
import org.kamiblue.client.util.TickTimer
import org.kamiblue.event.listener.listener
import java.net.InetSocketAddress
import java.net.Socket

object NetworkManager : Manager {

    var isOffline = false; private set

    private val lastUpdateTimer = TickTimer()

    init {
        listener<TickEvent.ClientTickEvent> {
            if (lastUpdateTimer.tick(1500L)) {
                isOffline = try {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress("1.1.1.1", 80), 100)
                        false
                    }
                } catch (e: Exception) {
                    true // Either timeout or unreachable or failed DNS lookup.
                }
            }
        }
    }
}