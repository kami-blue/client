package me.zeroeightsix.kami.module.modules.misc

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.GuiScreenEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.client.gui.GuiDisconnected
import net.minecraft.client.multiplayer.GuiConnecting
import net.minecraft.client.multiplayer.ServerData
import kotlin.math.floor
import kotlin.math.max

/**
 * Created by 086 on 9/04/2018.
 * Updated by Xiaro on 09/09/20
 */
@Module.Info(
        name = "AutoReconnect",
        description = "Automatically reconnects after being disconnected",
        category = Module.Category.MISC,
        alwaysListening = true,
        showOnArray = Module.ShowOnArray.OFF
)
class AutoReconnect : Module() {
    private val seconds = register(Settings.doubleBuilder("Seconds").withValue(5.0).withMinimum(0.5).build())

    private var cServer: ServerData? = null

    @EventHandler
    private val closedListener = Listener(EventHook { event: GuiScreenEvent.Closed ->
        if (event.screen is GuiConnecting) cServer = mc.currentServerData
    })

    @EventHandler
    private val displayedListener = Listener(EventHook { event: GuiScreenEvent.Displayed ->
        if (isEnabled || (cServer == null && mc.currentServerData == null))
            (event.screen as? GuiDisconnected)?.let {
                event.screen = KamiGuiDisconnected(it)
            }
    })

    private inner class KamiGuiDisconnected(disconnected: GuiDisconnected) : GuiDisconnected(disconnected.parentScreen, disconnected.reason, disconnected.message) {
        private var millis = seconds.value * 1000
        private var cTime: Long

        override fun updateScreen() {
            if (millis <= 0) mc.displayGuiScreen(GuiConnecting(parentScreen, mc, mc.currentServerData ?: cServer!!))
        }

        override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
            super.drawScreen(mouseX, mouseY, partialTicks)
            val a = System.currentTimeMillis()
            millis -= a - cTime.toDouble()
            cTime = a
            val s = "Reconnecting in ${max(0.0, floor(millis / 100) / 10)}s"
            fontRenderer.drawString(s, width / 2 - fontRenderer.getStringWidth(s) / 2.toFloat(), height - 16.toFloat(), 0xffffff, true)
        }

        init {
            cTime = System.currentTimeMillis()
        }
    }
}