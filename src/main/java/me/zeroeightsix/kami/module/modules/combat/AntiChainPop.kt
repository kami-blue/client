package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.InventoryUtils
import net.minecraft.network.play.server.SPacketEntityStatus
import org.kamiblue.event.listener.listener

@Module.Info(
        name = "AntiChainPop",
        description = "Enables Surround when popping a totem",
        category = Module.Category.COMBAT
)
object AntiChainPop : Module() {
    private val mode = register(Settings.e<Mode>("Mode", Mode.PACKET))

    private enum class Mode {
        ITEMS, PACKET
    }

    private var totems = 0

    init {
        listener<PacketEvent.Receive> { event ->
            if (mode.value != Mode.PACKET || event.packet !is SPacketEntityStatus || event.packet.opCode.toInt() != 35) return@listener
            mc.world?.let {
                if (event.packet.getEntity(it) == mc.player) {
                    Surround.enable()
                }
            }
        }

        listener<SafeTickEvent> {
            if (mode.value == Mode.ITEMS) return@listener
            val old = totems
            val new = InventoryUtils.countItemAll(449)
            if (new < old) Surround.enable()
            totems = new
        }
    }

    override fun onToggle() {
        totems = 0
    }
}