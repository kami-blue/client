package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.EntityUtils.getInterpolatedPos
import me.zeroeightsix.kami.util.graphics.KamiTessellator
import me.zeroeightsix.kami.util.math.VectorUtils.distanceTo
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.realms.RealmsMth.sin
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import org.lwjgl.opengl.GL11.GL_LINE_STRIP
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayDeque
import kotlin.collections.HashMap
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

object Breadcrumbs : Module(
    name = "Breadcrumbs",
    description = "Draws a tail behind as you move",
    category = Category.RENDER,
    alwaysListening = true
) {
    private val clear = setting("Clear", false)
    private val whileDisabled = setting("WhileDisabled", false)
    private val smoothFactor = setting("SmoothFactor", 5.0f, 0.0f..10.0f, 0.25f)
    private val maxDistance = setting("MaxDistance", 4096, 1024..16384, 1024)
    private val yOffset = setting("YOffset", 0.5f, 0.0f..1.0f, 0.05f)
    private val throughBlocks = setting("ThroughBlocks", true)
    private val r = setting("Red", 255, 0..255, 1)
    private val g = setting("Green", 166, 0..255, 1)
    private val b = setting("Blue", 188, 0..255, 1)
    private val a = setting("Alpha", 200, 0..255, 1)
    private val thickness = setting("LineThickness", 2.0f, 0.25f..8.0f, 0.25f)

    private val mainList = ConcurrentHashMap<String, HashMap<Int, ArrayDeque<Vec3d>>>() /* <Server IP, <Dimension, PositionList>> */
    private var prevDimension = -2
    private var startTime = -1L
    private var alphaMultiplier = 0f
    private var tickCount = 0

    init {
        onToggle {
            if (!whileDisabled.value) {
                mainList.clear()
            }
        }

        listener<ConnectionEvent.Disconnect> {
            startTime = 0L
            alphaMultiplier = 0f
        }

        safeListener<RenderWorldEvent> {
            if ((mc.integratedServer == null && mc.currentServerData == null) || (isDisabled && !whileDisabled.value)) {
                return@safeListener
            }

            if (player.dimension != prevDimension) {
                startTime = 0L
                alphaMultiplier = 0f
                prevDimension = player.dimension
            }
            if (!shouldRecord(true)) return@safeListener

            /* Adding server and dimension to the map if they are not exist */
            val serverIP = getServerIP()
            val dimension = player.dimension

            /* Adding position points to list */
            val renderPosList = addPos(serverIP, dimension, KamiTessellator.pTicks())

            /* Rendering */
            drawTail(renderPosList)
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START || mc.integratedServer == null && mc.currentServerData == null) return@safeListener

            alphaMultiplier = if (isEnabled && shouldRecord(false)) min(alphaMultiplier + 0.07f, 1f)
            else max(alphaMultiplier - 0.05f, 0f)

            if (isDisabled && !whileDisabled.value) return@safeListener

            if (tickCount < 200) {
                tickCount++
            } else {
                val serverIP = getServerIP()
                val posList = mainList.getOrPut(serverIP, ::HashMap).getOrPut(player.dimension, ::ArrayDeque)

                val cutoffPos = posList.lastOrNull { pos -> player.distanceTo(pos) > maxDistance.value }
                if (cutoffPos != null) {
                    while (posList.first() != cutoffPos) {
                        posList.removeFirstOrNull()
                    }
                }

                mainList.getOrPut(serverIP, ::HashMap)[player.dimension] = posList
                tickCount = 0
            }
        }
    }

    private fun drawTail(posList: LinkedList<Vec3d>) {
        if (posList.isNotEmpty() && alphaMultiplier != 0.0f) {
            val offset = Vec3d(0.0, yOffset.value + 0.05, 0.0)
            val buffer = KamiTessellator.buffer
            GlStateManager.depthMask(!throughBlocks.value)
            GlStateManager.glLineWidth(thickness.value)
            KamiTessellator.begin(GL_LINE_STRIP)
            for (pos in posList) {
                val offsetPost = pos.add(offset)
                buffer.pos(offsetPost.x, offsetPost.y, offsetPost.z).color(r.value, g.value, b.value, (a.value * alphaMultiplier).toInt()).endVertex()
            }
            KamiTessellator.render()
        }
    }

    private fun SafeClientEvent.addPos(serverIP: String, dimension: Int, pTicks: Float): LinkedList<Vec3d> {
        var minDist = sin(-0.05f * smoothFactor.value * PI.toFloat()) * 2f + 2.01f
        if (isDisabled) minDist *= 2f
        var currentPos = getInterpolatedPos(player, pTicks)
        if (player.isElytraFlying) currentPos = currentPos.subtract(0.0, 0.5, 0.0)

        val posList = mainList.getOrPut(serverIP, ::HashMap).getOrPut(dimension, ::ArrayDeque)

        /* Adds position only when the list is empty or the distance between current position and the last position is further than the min distance */
        if (posList.isEmpty() || currentPos.distanceTo(posList.last()) > minDist) {
            posList.add(currentPos)
        }

        val returningList = LinkedList<Vec3d>(posList) /* Makes a copy of the list */
        returningList.add(currentPos) /* Adds current position to the copied list */
        return returningList
    }

    private fun getServerIP(): String {
        return mc.currentServerData?.serverIP
            ?: mc.integratedServer?.worldName ?: ""
    }

    private fun shouldRecord(reset: Boolean): Boolean {
        return if (startTime == 0L) {
            if (reset) startTime = System.currentTimeMillis()
            false
        } else System.currentTimeMillis() - startTime > 1000L
    }

    init {
        clear.listeners.add {
            if (clear.value) {
                mainList.clear()
                sendChatMessage("$chatName Cleared!")
                clear.value = false
            }
        }
    }
}