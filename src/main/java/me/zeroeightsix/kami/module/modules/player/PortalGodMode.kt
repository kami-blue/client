package me.zeroeightsix.kami.module.modules.player

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
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
            mc.networkManager?.sendPacket(packet as Packet<INetHandlerPlayServer>)
        }
    }

    @EventHandler
    private val listener = Listener(EventHook { event: PacketEvent.Send ->
        if (event.packet is CPacketConfirmTeleport) {
            event.cancel()
            packet = event.packet
        }
    })
}