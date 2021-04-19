package org.kamiblue.client.module.modules.player

import net.minecraft.client.gui.ScaledResolution
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.events.ConnectionEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.event.events.RenderOverlayEvent
import org.kamiblue.client.manager.managers.NetworkManager
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.process.PauseProcess.pauseBaritone
import org.kamiblue.client.process.PauseProcess.unpauseBaritone
import org.kamiblue.client.util.*
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.graphics.font.FontRenderAdapter
import org.kamiblue.client.util.math.Vec2f
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.commons.utils.MathUtils
import org.kamiblue.event.listener.listener
import org.lwjgl.opengl.GL11.glColor4f

/**
 * Thanks Brady and cooker and leij for helping me not be completely retarded
 */
internal object LagNotifier : Module(
    name = "LagNotifier",
    description = "Displays a warning when the server is lagging",
    category = Category.PLAYER
) {
    private val detectRubberBand by setting("Detect Rubber Band", true)
    private val pauseBaritone by setting("Pause Baritone", true)
    val pauseTakeoff by setting("Pause Elytra Takeoff", true)
    val pauseAutoWalk by setting("Pause Auto Walk", true)
    private val feedback by setting("Pause Feedback", true, { pauseBaritone })
    private val timeout by setting("Timeout", 3.5f, 0.0f..10.0f, 0.5f)

    private val pingTimer = TickTimer(TimeUnit.SECONDS)
    private val lastPacketTimer = TickTimer()
    private val lastRubberBandTimer = TickTimer()
    private var text = ""

    var paused = false; private set

    init {
        onDisable {
            unpause()
        }

        listener<RenderOverlayEvent> {
            if (text.isBlank()) return@listener

            val resolution = ScaledResolution(mc)
            val posX = resolution.scaledWidth / 2.0f - FontRenderAdapter.getStringWidth(text) / 2.0f
            val posY = 80.0f / resolution.scaleFactor

            /* 80px down from the top edge of the screen */
            FontRenderAdapter.drawString(text, posX, posY, color = ColorHolder(255, 33, 33))
            glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (mc.isIntegratedServerRunning) {
                unpause()
                text = ""
            } else {
                val timeoutMillis = (timeout * 1000.0f).toLong()
                when {
                    lastPacketTimer.tick(timeoutMillis, false) -> {
                        text = if (NetworkManager.isOffline) "Your internet is offline! "
                        else "Server Not Responding! "

                        text += timeDifference(lastPacketTimer.time)
                        pause()
                    }
                    detectRubberBand && !lastRubberBandTimer.tick(timeoutMillis, false) -> {
                        text = "RubberBand Detected! ${timeDifference(lastRubberBandTimer.time)}"
                        pause()
                    }
                    else -> {
                        unpause()
                    }
                }
            }
        }

        safeListener<PacketEvent.Receive>(2000) {
            lastPacketTimer.reset()

            if (!detectRubberBand || it.packet !is SPacketPlayerPosLook || player.ticksExisted < 20) return@safeListener

            val dist = Vec3d(it.packet.x, it.packet.y, it.packet.z).subtract(player.positionVector).length()
            val rotationDiff = Vec2f(it.packet.yaw, it.packet.pitch).minus(Vec2f(player)).length()

            if (dist in 0.5..64.0 || rotationDiff > 1.0) lastRubberBandTimer.reset()
        }

        listener<ConnectionEvent.Connect> {
            lastPacketTimer.reset(69420L)
            lastRubberBandTimer.reset(-69420L)
        }
    }

    private fun pause() {
        if (!paused && pauseBaritone && feedback) {
            MessageSendHelper.sendBaritoneMessage("Paused due to lag!")
        }

        pauseBaritone()
        paused = true
    }

    private fun unpause() {
        if (paused && pauseBaritone && feedback) {
            MessageSendHelper.sendBaritoneMessage("Unpaused!")
        }

        unpauseBaritone()
        paused = false
        text = ""
    }

    private fun timeDifference(timeIn: Long) = MathUtils.round((System.currentTimeMillis() - timeIn) / 1000.0, 1)
}
