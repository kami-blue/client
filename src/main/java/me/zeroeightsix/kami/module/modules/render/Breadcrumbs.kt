package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.EntityUtils.getInterpolatedPos
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.graphics.KamiTessellator
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.realms.RealmsMth.sin
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.GL11.GL_LINE_STRIP
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

@Module.Info(
        name = "Breadcrumbs",
        description = "Draws a tail behind as you move",
        category = Module.Category.RENDER,
        alwaysListening = true
)
object Breadcrumbs : Module() {
    private val clear = register(Settings.b("Clear", false))
    private val whileDisabled = register(Settings.b("WhileDisabled", false))
    private val smoothFactor = register(Settings.floatBuilder("SmoothFactor").withValue(5.0f).withRange(0.0f, 10.0f).withStep(0.25f))
    private val maxDistance = register(Settings.integerBuilder("MaxDistance").withValue(4096).withRange(1024, 16384).withStep(1024))
    private val yOffset = register(Settings.floatBuilder("YOffset").withValue(0.5f).withRange(0.0f, 1.0f).withStep(0.05f))
    private val throughBlocks = register(Settings.b("ThroughBlocks", true))
    private val r = register(Settings.integerBuilder("Red").withValue(255).withRange(0, 255).withStep(1))
    private val g = register(Settings.integerBuilder("Green").withValue(166).withRange(0, 255).withStep(1))
    private val b = register(Settings.integerBuilder("Blue").withValue(188).withRange(0, 255).withStep(1))
    private val a = register(Settings.integerBuilder("Alpha").withValue(200).withRange(0, 255).withStep(1))
    private val thickness = register(Settings.floatBuilder("LineThickness").withValue(2.0f).withRange(0.25f, 8.0f).withStep(0.25f))

    private val mainList = ConcurrentHashMap<String, HashMap<Int, LinkedList<Vec3d>>>() /* <Server IP, <Dimension, PositionList>> */
    private var prevDimension = -2
    private var startTime = -1L
    private var alphaMultiplier = 0f
    private var tickCount = 0

    override fun onToggle() {
        if (!whileDisabled.value) {
            mainList.clear()
        }
    }

    init {
        listener<ConnectionEvent.Disconnect> {
            startTime = 0L
            alphaMultiplier = 0f
        }

        listener<RenderWorldEvent> {
            if (mc.player == null || (mc.integratedServer == null && mc.currentServerData == null)
                    || (isDisabled && !whileDisabled.value)) {
                return@listener
            }
            if (mc.player.dimension != prevDimension) {
                startTime = 0L
                alphaMultiplier = 0f
                prevDimension = mc.player.dimension
            }
            if (!shouldRecord(true)) return@listener

            /* Adding server and dimension to the map if they are not exist */
            val serverIP = getServerIP()
            val dimension = mc.player.dimension
            if (!mainList.containsKey(serverIP)) { /* Add server to the map if not exist */
                mainList[serverIP] = hashMapOf(Pair(dimension, LinkedList()))
            } else if (!mainList[serverIP]!!.containsKey(dimension)) { /* Add dimension to the map if not exist */
                mainList[serverIP]!![dimension] = LinkedList()
            }

            /* Adding position points to list */
            val renderPosList = addPos(serverIP, dimension, KamiTessellator.pTicks())

            /* Rendering */
            drawTail(renderPosList)
        }

        listener<SafeTickEvent> {
            if (it.phase != TickEvent.Phase.START || mc.integratedServer == null && mc.currentServerData == null) return@listener

            alphaMultiplier = if (isEnabled && shouldRecord(false)) min(alphaMultiplier + 0.07f, 1f)
            else max(alphaMultiplier - 0.05f, 0f)

            if (isDisabled && !whileDisabled.value) return@listener

            if (tickCount < 200) {
                tickCount++
            } else {
                val serverIP = getServerIP()
                val dimension = mc.player.dimension
                val posList = ((mainList[serverIP] ?: return@listener)[dimension] ?: return@listener)
                val cutoffPos = posList.lastOrNull { pos -> mc.player.getDistance(pos.x, pos.y, pos.z) > maxDistance.value }
                if (cutoffPos != null) while (posList.first() != cutoffPos) {
                    posList.remove()
                }
                mainList[serverIP]!![dimension] = posList
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

    private fun addPos(serverIP: String, dimension: Int, pTicks: Float): LinkedList<Vec3d> {
        var minDist = sin(-0.05f * smoothFactor.value * PI.toFloat()) * 2f + 2.01f
        if (isDisabled) minDist *= 2f
        var currentPos = getInterpolatedPos(mc.player, pTicks)
        if (mc.player.isElytraFlying) currentPos = currentPos.subtract(0.0, 0.5, 0.0)
        val posList = mainList[serverIP]!![dimension]!!

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
        clear.settingListener = Setting.SettingListeners {
            if (clear.value) {
                mainList.clear()
                sendChatMessage("$chatName Cleared!")
                clear.value = false
            }
        }
    }
}