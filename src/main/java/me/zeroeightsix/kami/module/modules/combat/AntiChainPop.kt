package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.items.allSlots
import me.zeroeightsix.kami.util.items.countItem
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.init.Items
import net.minecraft.network.play.server.SPacketEntityStatus
import net.minecraftforge.fml.common.gameevent.TickEvent

object AntiChainPop : Module(
    name = "AntiChainPop",
    description = "Enables Surround when popping a totem",
    category = Category.COMBAT
) {
    private val mode by setting("Mode", Mode.PACKET)

    private enum class Mode {
        ITEMS, PACKET
    }

    private var totems = 0

    init {
        safeListener<PacketEvent.Receive> { event ->
            if (mode != Mode.PACKET || event.packet !is SPacketEntityStatus || event.packet.opCode.toInt() != 35) return@safeListener
            if (event.packet.getEntity(world) == mc.player) {
                Surround.enable()
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (mode == Mode.ITEMS) return@safeListener
            val old = totems
            val new = player.allSlots.countItem(Items.TOTEM_OF_UNDYING)
            if (new < old) Surround.enable()
            totems = new
        }

        onDisable {
            totems = 0
        }
    }
}