package org.kamiblue.client.module.modules.player

import net.minecraft.network.play.client.CPacketAnimation
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.event.listener.listener

internal object NoSwing : Module(
    name = "NoSwing",
    category = Category.PLAYER,
    description = "Cancels server or client swing animation"
) {
    private val mode = setting("Mode", Mode.CLIENT)

    private enum class Mode {
        CLIENT, SERVER
    }

    init {
        listener<PacketEvent.Send> {
            if (mode.value == Mode.SERVER && it.packet is CPacketAnimation) it.cancel()
        }

        safeListener<TickEvent.ClientTickEvent> {
            player.isSwingInProgress = false
            player.swingProgressInt = 0
            player.swingProgress = 0.0f
            player.prevSwingProgress = 0.0f
        }
    }
}