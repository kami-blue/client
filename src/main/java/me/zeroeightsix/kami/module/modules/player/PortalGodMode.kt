package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.threads.runSafe
import net.minecraft.network.play.client.CPacketConfirmTeleport
import org.kamiblue.event.listener.listener

@Module.Info(
        name = "PortalGodMode",
        category = Module.Category.PLAYER,
        description = "Don't take damage in portals"
)
object PortalGodMode : Module() {
    private val confirm by setting("InstantTeleport", true)

    private var packet: CPacketConfirmTeleport? = null

    init {
        onEnable {
            packet = null
        }

        onDisable {
            runSafe {
                if (confirm) packet?.let {
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