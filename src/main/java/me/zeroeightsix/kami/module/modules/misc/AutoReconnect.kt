package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.events.GuiEvent
import me.zeroeightsix.kami.mixin.extension.message
import me.zeroeightsix.kami.mixin.extension.parentScreen
import me.zeroeightsix.kami.mixin.extension.reason
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.StopTimer
import net.minecraft.client.gui.GuiDisconnected
import net.minecraft.client.multiplayer.GuiConnecting
import net.minecraft.client.multiplayer.ServerData
import org.kamiblue.event.listener.listener
import kotlin.math.max

@Module.Info(
    name = "AutoReconnect",
    description = "Automatically reconnects after being disconnected",
    category = Module.Category.MISC,
    alwaysListening = true
)
object AutoReconnect : Module() {
    private val delay = setting("Delay", 5.0f, 0.5f..100.0f, 0.5f)

    private var prevServerDate: ServerData? = null

    init {
        listener<GuiEvent.Closed> {
            if (it.screen is GuiConnecting) prevServerDate = mc.currentServerData
        }

        listener<GuiEvent.Displayed> {
            if (isDisabled || (prevServerDate == null && mc.currentServerData == null)) return@listener
            (it.screen as? GuiDisconnected)?.let { gui ->
                it.screen = KamiGuiDisconnected(gui)
            }
        }
    }

    private class KamiGuiDisconnected(disconnected: GuiDisconnected) : GuiDisconnected(disconnected.parentScreen, disconnected.reason, disconnected.message) {
        private val timer = StopTimer()

        override fun updateScreen() {
            if (timer.stop() >= (delay.value * 1000.0f)) {
                mc.displayGuiScreen(GuiConnecting(parentScreen, mc, mc.currentServerData ?: prevServerDate ?: return))
            }
        }

        override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
            super.drawScreen(mouseX, mouseY, partialTicks)
            val ms = max(delay.value * 1000.0f - timer.stop(), 0.0f).toInt()
            val text = "Reconnecting in ${ms}ms"
            fontRenderer.drawString(text, width / 2f - fontRenderer.getStringWidth(text) / 2f, height - 32f, 0xffffff, true)
        }
    }
}
