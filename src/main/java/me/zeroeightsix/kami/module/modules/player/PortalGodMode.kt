package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.threads.runSafe
import net.minecraft.network.play.client.CPacketConfirmTeleport
import org.kamiblue.event.listener.listener

internal object PortalGodMode : Module(
    name = "PortalGodMode",
    category = Category.PLAYER,
    description = "Don't take damage in portals"
) {
    private val instantTeleport by setting("InstantTeleport", true)

    private var packet: CPacketConfirmTeleport? = null

    init {
        onEnable {
            packet = null
        }

        onDisable {
            runSafe {
                if (instantTeleport) packet?.let {
                    connection.sendPacket(it)
                }
            }
        }

        listener<PacketEvent.Send> {
            if (it.packet !is CPacketConfirmTeleport) return@listener
            it.cancel()
            packet = it.packet
        }
    }
}