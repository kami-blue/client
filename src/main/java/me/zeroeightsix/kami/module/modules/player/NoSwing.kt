package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import org.kamiblue.event.listener.listener
import net.minecraft.network.play.client.CPacketAnimation

@Module.Info(
        name = "NoSwing",
        category = Module.Category.PLAYER,
        description = "Cancels server or client swing animation"
)
object NoSwing : Module() {
    private val mode = register(Settings.e<Mode>("Mode", Mode.CLIENT))

    private enum class Mode {
        CLIENT, SERVER
    }

    init {
        listener<PacketEvent.Send> {
            if (mode.value == Mode.SERVER && it.packet is CPacketAnimation) it.cancel()
        }

        listener<SafeTickEvent> {
            mc.player.isSwingInProgress = false
            mc.player.swingProgressInt = 0
            mc.player.swingProgress = 0.0f
            mc.player.prevSwingProgress = 0.0f
        }
    }
}