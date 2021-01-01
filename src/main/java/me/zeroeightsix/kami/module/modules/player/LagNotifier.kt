package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.RenderOverlayEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.WebUtils
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter
import me.zeroeightsix.kami.util.math.Vec2f
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.math.Vec3d
import org.kamiblue.commons.utils.MathUtils
import org.kamiblue.event.listener.listener
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
    private val detectRubberBand = register(Settings.b("DetectRubberBand", true))

    private val pauseBaritone = register(Settings.b("PauseBaritone", true))
    val pauseTakeoff = register(Settings.b("PauseElytraTakeoff", true))
    val pauseAutoWalk = register(Settings.b("PauseAutoWalk", true))

    private val feedback = register(Settings.booleanBuilder("PauseFeedback").withValue(true).withVisibility { pauseBaritone.value })
    private val timeout = register(Settings.floatBuilder("Timeout").withValue(3.5f).withRange(0.0f, 10.0f))

    private val pingTimer = TimerUtils.TickTimer(TimerUtils.TimeUnit.SECONDS)
    private var lastPacketTimer = TimerUtils.TickTimer()
    private var lastRubberBandTimer = TimerUtils.TickTimer()
    private var text = ""
    var paused = false

    override fun onDisable() {
        unpause()
    }

    init {
        listener<RenderOverlayEvent> {
            if (text.isBlank()) return@listener

            val resolution = ScaledResolution(mc)
            val posX = resolution.scaledWidth / 2.0f - FontRenderAdapter.getStringWidth(text) / 2.0f
            val posY = 80.0f / resolution.scaleFactor

            /* 80px down from the top edge of the screen */
            FontRenderAdapter.drawString(text, posX, posY, color = ColorHolder(255, 33, 33))
            glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        }

        listener<SafeTickEvent> {
            if ((mc.currentScreen != null && mc.currentScreen !is GuiChat) || mc.isIntegratedServerRunning) {
                if (mc.isIntegratedServerRunning) unpause()
                text = ""
            } else {
                val timeoutMillis = (timeout.value * 1000.0f).toLong()
                when {
                    lastPacketTimer.tick(timeoutMillis, false) -> {
                        if (pingTimer.tick(1L)) WebUtils.update()
                        text = if (WebUtils.isInternetDown) "Your internet is offline! " else "Server Not Responding! "
                        text += timeDifference(lastPacketTimer.time)
                        pause()
                    }
                    detectRubberBand.value && !lastRubberBandTimer.tick(timeoutMillis, false) -> {
                        text = "RubberBand Detected! ${timeDifference(lastRubberBandTimer.time)}"
                        pause()
                    }
                    else -> {
                        unpause()
                        mc.currentScreen
                    }
                }
            }
        }

        listener<PacketEvent.Receive>(2000) {
            lastPacketTimer.reset()

            if (!detectRubberBand.value || mc.player == null || it.packet !is SPacketPlayerPosLook) return@listener

            val dist = Vec3d(it.packet.x, it.packet.y, it.packet.z).subtract(mc.player.positionVector).length()
            val rotationDiff = Vec2f(it.packet.yaw, it.packet.pitch).subtract(Vec2f(mc.player)).length()

            if (dist in 0.5..64.0 || rotationDiff > 1.0) lastRubberBandTimer.reset()
        }

        listener<ConnectionEvent.Connect> {
            lastPacketTimer.reset(5000L)
            lastRubberBandTimer.reset(5000L)
        }
    }

    private fun pause() {
        if (pauseBaritone.value && !paused) {
            if (feedback.value) MessageSendHelper.sendBaritoneMessage("Paused due to lag!")
            BaritoneUtils.pause()
        }
        if (pauseTakeoff.value || pauseAutoWalk.value) paused = true
    }

    private fun unpause() {
        if (BaritoneUtils.paused && paused) {
            if (feedback.value) MessageSendHelper.sendBaritoneMessage("Unpaused!")
            BaritoneUtils.unpause()
        }
        paused = false
        text = ""
    }

    private fun timeDifference(timeIn: Long) = MathUtils.round((System.currentTimeMillis() - timeIn) / 1000.0, 1)
}
