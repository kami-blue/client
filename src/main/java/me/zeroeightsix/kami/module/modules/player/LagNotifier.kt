package me.zeroeightsix.kami.module.modules.player

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.PacketEvent.Receive
import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.client.Baritone
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MathsUtils
import me.zeroeightsix.kami.util.WebHelper
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.client.gui.GuiChat
import baritone.api.BaritoneAPI
import me.zeroeightsix.kami.util.BaritoneUtils

/**
 * @author dominikaaaa
 * Thanks Brady and cooker and leij for helping me not be completely retarded
 *
 * Updated by dominikaaaa on 19/04/20
 */
@Module.Info(
        name = "LagNotifier",
        description = "Displays a warning when the server is lagging",
        category = Module.Category.PLAYER
)
class LagNotifier : Module() {
    var pauseDuringLag = register(Settings.b("Pause Baritone", false))
    private val timeout = register(Settings.doubleBuilder().withName("Timeout").withValue(2.0).withMinimum(0.0).withMaximum(10.0).build())

    private var serverLastUpdated: Long = 0
    var text = "Server Not Responding! "

    var isLagging = false

    override fun onRender() {
        if (mc.currentScreen != null && mc.currentScreen !is GuiChat) return
        if (1000L *  timeout.value.toDouble() > System.currentTimeMillis() - serverLastUpdated) {
            isLagging = false

            if (pauseDuringLag.value) {
                BaritoneUtils.unpause()
            }

            return
        }

        if (shouldPing()) {
            text = if (WebHelper.isDown("1.1.1.1", 80, 1000)) {
                "Your internet is offline! "
            } else {
                "Server Not Responding! "
            }
        }
        text = text.replace("! .*".toRegex(), "! " + timeDifference() + "s")
        val renderer = Wrapper.getFontRenderer()
        val divider = DisplayGuiScreen.getScale()

        /* 217 is the offset to make it go high, bigger = higher, with 0 being center */
        renderer.drawStringWithShadow(mc.displayWidth / divider / 2 - renderer.getStringWidth(text) / 2, mc.displayHeight / divider / 2 - 217, 255, 85, 85, text)

        isLagging = true

        if (pauseDuringLag.value)
        {
            BaritoneUtils.pause()
        }
    }

    @EventHandler
    private val receiveListener = Listener(EventHook { event: Receive? -> serverLastUpdated = System.currentTimeMillis() })

    private fun timeDifference(): Double {
        return MathsUtils.round((System.currentTimeMillis() - serverLastUpdated) / 1000.0, 1)
    }

    private fun shouldPing(): Boolean {
        if (startTime == 0L) startTime = System.currentTimeMillis()
        if (startTime + 1000 <= System.currentTimeMillis()) { // 1 second
            startTime = System.currentTimeMillis()
            return true
        }
        return false
    }

    companion object {
        private var startTime: Long = 0
    }
}