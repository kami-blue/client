package me.zeroeightsix.kami.module.modules.combat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.client.InfoOverlay
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.init.Items
import net.minecraft.network.play.server.SPacketEntityStatus

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 25/03/20
 *
 * Event / Packet mode taken from CliNet
 * https://github.com/DarkiBoi/CliNet/blob/fd225a5c8cc373974b0c9a3457acbeed206e8cca/src/main/java/me/zeroeightsix/kami/module/modules/combat/TotemPopCounter.java
 */
@Module.Info(
        name = "AntiChainPop",
        description = "Enables Surround when popping a totem",
        category = Module.Category.COMBAT
)
class AntiChainPop : Module() {
    private val mode = register(Settings.e<Mode>("Mode", Mode.PACKET))

    private var totems = 0

    @EventHandler
    private val selfPopListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (mc.player == null || mode.value != Mode.PACKET) return@EventHook

        if (event.packet is SPacketEntityStatus) {
            val packet = event.packet as SPacketEntityStatus
            if (packet.opCode.toInt() == 35) {
                val entity = packet.getEntity(mc.world)
                if (entity.displayName == mc.player.displayName) packetMode()
            }
        }
    })

    override fun onUpdate() {
        if (mc.player == null) return
        if (mode.value == Mode.ITEMS) {
            itemMode()
        }
    }

    private fun itemMode() {
        val old = totems
        if (InfoOverlay.getItems(Items.TOTEM_OF_UNDYING) < old) {
            val surround = KamiMod.MODULE_MANAGER.getModuleT(Surround::class.java)!!
            surround.autoDisable.value = true
            surround.enable()
        }
        totems = InfoOverlay.getItems(Items.TOTEM_OF_UNDYING)
    }

    private fun packetMode() {
        val surround = KamiMod.MODULE_MANAGER.getModuleT(Surround::class.java)!!
        surround.autoDisable.value = true
        surround.enable()
    }

    public override fun onToggle() {
        totems = 0
    }

    private enum class Mode {
        ITEMS, PACKET
    }
}