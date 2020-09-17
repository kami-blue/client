package me.zeroeightsix.kami.module.modules.combat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.InventoryUtils
import net.minecraft.network.play.server.SPacketEntityStatus

@Module.Info(
        name = "AntiChainPop",
        description = "Enables Surround when popping a totem",
        category = Module.Category.COMBAT
)
object AntiChainPop : Module() {
    private val mode = register(Settings.e<Mode>("Mode", Mode.PACKET))

    private var totems = 0

    @EventHandler
    private val selfPopListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (mc.player == null || mode.value != Mode.PACKET) return@EventHook

        if (event.packet is SPacketEntityStatus) {
            val packet = event.packet
            if (packet.opCode.toInt() == 35) {
                val entity = packet.getEntity(mc.world)
                if (entity.displayName == mc.player.displayName) packetMode()
            }
        }
    })

    override fun onUpdate() {
        if (mode.value == Mode.ITEMS) {
            itemMode()
        }
    }

    private fun itemMode() {
        val old = totems
        if (InventoryUtils.countItemAll(449) < old) {
            Surround.autoDisable.value = true
            Surround.enable()
        }
        totems = InventoryUtils.countItemAll(449)
    }

    private fun packetMode() {
        Surround.autoDisable.value = true
        Surround.enable()
    }

    public override fun onToggle() {
        totems = 0
    }

    private enum class Mode {
        ITEMS, PACKET
    }
}