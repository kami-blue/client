package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.events.GuiScreenEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.client.gui.GuiDisconnected
import net.minecraft.client.multiplayer.GuiConnecting
import net.minecraft.client.multiplayer.ServerData

@Module.Info(
        name = "AutoReconnect",
        description = "Automatically reconnects after being disconnected",
        category = Module.Category.MISC,
        alwaysListening = true
)
object AutoReconnect : Module() {
    private val delay = register(Settings.integerBuilder("Delay").withValue(5000).withRange(100, 10000).withStep(100))

    private var prevServerDate: ServerData? = null

    init {
        listener<GuiScreenEvent.Closed> {
            if (it.screen is GuiConnecting) prevServerDate = mc.currentServerData
        }

        listener<GuiScreenEvent.Displayed> {
            if (isDisabled || (prevServerDate == null && mc.currentServerData == null)) return@listener
            (it.screen as? GuiDisconnected)?.let { gui ->
                it.screen = KamiGuiDisconnected(gui)
            }
        }
    }

    private class KamiGuiDisconnected(disconnected: GuiDisconnected) : GuiDisconnected(disconnected.parentScreen, disconnected.reason, disconnected.message) {
        private val timer = TimerUtils.StopTimer()

        override fun updateScreen() {
            if (timer.stop() >= delay.value) {
                mc.displayGuiScreen(GuiConnecting(parentScreen, mc, mc.currentServerData ?: prevServerDate ?: return))
            }
        }

        override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
            super.drawScreen(mouseX, mouseY, partialTicks)
            val text = "Reconnecting in ${timer.stop()}ms"
            fontRenderer.drawString(text, width / 2f - fontRenderer.getStringWidth(text) / 2f, height - 32f, 0xffffff, true)
        }
    }
}