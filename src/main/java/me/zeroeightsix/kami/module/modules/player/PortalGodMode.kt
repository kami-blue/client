package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.network.play.client.CPacketConfirmTeleport

@Module.Info(
        name = "PortalGodMode",
        category = Module.Category.PLAYER,
        description = "Don't take damage in portals"
)
object PortalGodMode : Module() {
    private val confirm = register(Settings.b("InstantTeleport"))

    private var packet: CPacketConfirmTeleport? = null

    override fun onEnable() {
        packet = null
    }

    override fun onDisable() {
        if (confirm.value) packet?.let {
            mc.connection?.sendPacket(it)
        }
    }

    init {
        listener<PacketEvent.Send> {
            if (it.packet !is CPacketConfirmTeleport) return@listener
            it.cancel()
            packet = it.packet
        }
    }
}