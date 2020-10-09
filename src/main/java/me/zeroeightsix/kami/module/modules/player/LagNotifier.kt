package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.BaritoneUtils.pause
import me.zeroeightsix.kami.util.BaritoneUtils.unpause
import me.zeroeightsix.kami.util.WebHelper
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.math.MathUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.opengl.GL11.glColor4f

/**
 * Thanks Brady and cooker and leij for helping me not be completely retarded
 */
@Module.Info(
        name = "LagNotifier",
        description = "Displays a warning when the server is lagging",
        category = Module.Category.PLAYER
)
object LagNotifier : Module() {
    private val pauseTakeoff = register(Settings.b("PauseElytraTakeoff", true))
    private var pauseBaritone: Setting<Boolean> = register(Settings.b("PauseBaritone", true))
    private val feedback = register(Settings.booleanBuilder("PauseFeedback").withValue(true).withVisibility { pauseBaritone.value }.build())
    private val timeout = register(Settings.doubleBuilder().withName("Timeout").withValue(2.0).withMinimum(0.0).withMaximum(10.0).build())

    private var startTime: Long = 0
    private var serverLastUpdated: Long = 0
    var paused = false
    var text = "Server Not Responding! "

    override fun onRender() {
        if ((mc.currentScreen != null && mc.currentScreen !is GuiChat) || mc.isIntegratedServerRunning) return

        if (1000L * timeout.value.toDouble() > System.currentTimeMillis() - serverLastUpdated) {
            if (BaritoneUtils.paused && paused) {
                if (feedback.value) MessageSendHelper.sendBaritoneMessage("Unpaused!")
                unpause()
            }
            paused = false
            return
        }

        if (shouldPing()) {
            WebHelper.run()
            text = if (WebHelper.isInternetDown) {
                "Your internet is offline! "
            } else {
                "Server Not Responding! "
            }
            if (pauseBaritone.value && !paused) {
                if (feedback.value) MessageSendHelper.sendBaritoneMessage("Paused due to lag!")
                pause()
            }
            if (pauseTakeoff.value) paused = true
        }
        text = text.replace("! .*".toRegex(), "! " + timeDifference() + "s")
        val fontRenderer = mc.fontRenderer
        val resolution = ScaledResolution(mc)

        /* 80px down from the top edge of the screen */
        fontRenderer.drawStringWithShadow(text, resolution.scaledWidth / 2f - fontRenderer.getStringWidth(text) / 2f, 80f / resolution.scaleFactor, 0xff3333)
        glColor4f(1f, 1f, 1f, 1f)
    }

    override fun onDisable() {
        unpause()
    }

    init {
        listener<PacketEvent.Receive> {
            serverLastUpdated = System.currentTimeMillis()
        }
    }

    private fun timeDifference(): Double {
        return MathUtils.round((System.currentTimeMillis() - serverLastUpdated) / 1000.0, 1)
    }

    private fun shouldPing(): Boolean {
        if (startTime == 0L) startTime = System.currentTimeMillis()
        if (startTime + 1000 <= System.currentTimeMillis()) { // 1 second
            startTime = System.currentTimeMillis()
            return true
        }
        return false
    }
}