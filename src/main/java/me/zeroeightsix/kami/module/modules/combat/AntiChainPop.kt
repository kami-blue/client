package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.network.play.server.SPacketEntityStatus

@Module.Info(
        name = "AntiChainPop",
        description = "Enables Surround when popping a totem",
        category = Module.Category.COMBAT
)
object AntiChainPop : Module() {
    private val mode = register(Settings.e<Mode>("Mode", Mode.PACKET))

    private var totems = 0

    init {
        listener<PacketEvent.Receive> {
            if (mode.value != Mode.PACKET || it.packet !is SPacketEntityStatus || it.packet.opCode.toInt() != 35) return@listener
            mc.world?.let { world ->
                if (it.packet.getEntity(world) == mc.player) packetMode()
            }
        }
    }

    override fun onUpdate(event: SafeTickEvent) {
        if (mode.value == Mode.ITEMS) {
            itemMode()
        }
    }

    private fun itemMode() {
        val old = totems
        if (InventoryUtils.countItemAll(449) < old) {
            Surround.enable()
        }
        totems = InventoryUtils.countItemAll(449)
    }

    private fun packetMode() {
        Surround.enable()
    }

    public override fun onToggle() {
        totems = 0
    }

    private enum class Mode {
        ITEMS, PACKET
    }
}