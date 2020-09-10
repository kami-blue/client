package me.zeroeightsix.kami.module.modules.render

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.event.events.WaypointUpdateEvent
import me.zeroeightsix.kami.manager.mangers.FileInstanceManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.Waypoint
import me.zeroeightsix.kami.util.WaypointInfo
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.*
import me.zeroeightsix.kami.util.math.Vec2d
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11.*
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Created by Xiaro on 31/07/20.
 */
@Module.Info(
        name = "WaypointRender",
        description = "Render saved waypoints",
        category = Module.Category.RENDER
)
class WaypointRender : Module() {
    private val page = register(Settings.e<Page>("Page", Page.INFO_BOX))

    /* Page one */
    private val dimension = register(Settings.enumBuilder(Dimension::class.java).withName("Dimension").withValue(Dimension.CURRENT).withVisibility { page.value == Page.INFO_BOX })
    private val showName = register(Settings.booleanBuilder("ShowName").withValue(true).withVisibility { page.value == Page.INFO_BOX }.build())
    private val showDate = register(Settings.booleanBuilder("ShowDate").withValue(true).withVisibility { page.value == Page.INFO_BOX }.build())
    private val showCoords = register(Settings.booleanBuilder("ShowCoords").withValue(true).withVisibility { page.value == Page.INFO_BOX }.build())
    private val showDist = register(Settings.booleanBuilder("ShowDistance").withValue(true).withVisibility { page.value == Page.INFO_BOX }.build())
    private val textScale = register(Settings.floatBuilder("TextScale").withValue(1.0f).withRange(0.0f, 5.0f).withVisibility { page.value == Page.INFO_BOX }.build())
    private val infoBoxRange = register(Settings.integerBuilder("InfoBoxRange").withValue(512).withRange(128, 2048).withVisibility { page.value == Page.INFO_BOX }.build())

    /* Page two */
    private val espRangeLimit = register(Settings.booleanBuilder("RenderRange").withValue(true).withVisibility { page.value == Page.ESP }.build())
    private val espRange = register(Settings.integerBuilder("Range").withValue(4096).withRange(1024, 16384).withVisibility { page.value == Page.ESP && espRangeLimit.value }.build())
    private val filled = register(Settings.booleanBuilder("Filled").withValue(true).withVisibility { page.value == Page.ESP }.build())
    private val outline = register(Settings.booleanBuilder("Outline").withValue(true).withVisibility { page.value == Page.ESP }.build())
    private val tracer = register(Settings.booleanBuilder("Tracer").withValue(true).withVisibility { page.value == Page.ESP }.build())
    private val r = register(Settings.integerBuilder("Red").withMinimum(0).withValue(31).withMaximum(255).withVisibility { page.value == Page.ESP }.build())
    private val g = register(Settings.integerBuilder("Green").withMinimum(0).withValue(200).withMaximum(255).withVisibility { page.value == Page.ESP }.build())
    private val b = register(Settings.integerBuilder("Blue").withMinimum(0).withValue(63).withMaximum(255).withVisibility { page.value == Page.ESP }.build())
    private val aFilled = register(Settings.integerBuilder("FilledAlpha").withValue(63).withRange(0, 255).withVisibility { page.value == Page.ESP && filled.value }.build())
    private val aOutline = register(Settings.integerBuilder("OutlineAlpha").withValue(160).withRange(0, 255).withVisibility { page.value == Page.ESP && outline.value }.build())
    private val aTracer = register(Settings.integerBuilder("TracerAlpha").withValue(200).withRange(0, 255).withVisibility { page.value == Page.ESP && tracer.value }.build())
    private val thickness = register(Settings.floatBuilder("LineThickness").withValue(2.0f).withRange(0.0f, 8.0f).build())

    private enum class Dimension {
        CURRENT, ANY
    }

    private enum class Page {
        INFO_BOX, ESP
    }

    private val waypoints = ArrayList<WaypointInfo>()
    private var currentServer: String? = null
    private var ticks = 0

    override fun onWorldRender(event: RenderEvent) {
        if (mc.player == null || mc.renderManager.options == null || waypoints.isEmpty()) return
        val color = ColorHolder(r.value, g.value, b.value)
        val renderer = ESPRenderer()
        renderer.aFilled = if (filled.value) aFilled.value else 0
        renderer.aOutline = if (outline.value) aOutline.value else 0
        renderer.aTracer = if (tracer.value) aTracer.value else 0
        renderer.thickness = thickness.value
        for (waypoint in waypoints) {
            val pos = waypoint.currentPos()
            val distance = sqrt(mc.player.getDistanceSq(pos))
            if (espRangeLimit.value && distance > espRange.value) continue
            renderer.add(AxisAlignedBB(pos), color) /* Adds pos to ESPRenderer list */
            drawVerticalLines(pos, color, aOutline.value) /* Draw lines from y 0 to y 256 */
        }
        renderer.render(true)
    }

    override fun onRender() {
        if (!showCoords.value && !showName.value && !showDate.value && !showDist.value) return
        GlStateUtils.rescale(mc.displayWidth.toDouble(), mc.displayHeight.toDouble())
        for (waypoint in waypoints) {
            val pos = waypoint.currentPos()
            val distance = sqrt(mc.player.getDistanceSq(pos))
            if (distance > infoBoxRange.value) continue
            drawText(waypoint)
        }
        GlStateUtils.rescaleMc()
    }

    private fun drawVerticalLines(pos: BlockPos, color: ColorHolder, a: Int) {
        val box = AxisAlignedBB(pos.x.toDouble(), 0.0, pos.z.toDouble(),
                pos.x + 1.0, 256.0, pos.z + 1.0)
        KamiTessellator.begin(GL_LINES)
        KamiTessellator.drawOutline(box, color, a, GeometryMasks.Quad.ALL, thickness.value)
        KamiTessellator.render()
    }

    private fun drawText(waypoint: WaypointInfo) {
        glPushMatrix()
        glDisable(GL_TEXTURE_2D)

        val pos = Vec3d(waypoint.currentPos()).add(0.5, 0.5, 0.5)
        val screenPos = ProjectionUtils.toScreenPos(pos)
        glTranslated(screenPos.x, screenPos.y, 0.0)
        glScalef(textScale.value * 2f, textScale.value * 2f, 0f)

        var str = ""
        if (showName.value) str += "${'\n'}${waypoint.name}"
        if (showDate.value) str += "${'\n'}${waypoint.date}"
        if (showCoords.value) str += "${'\n'}${waypoint.asString(true)}"
        if (showDist.value) str += "${'\n'}${mc.player.getDistance(pos.x, pos.y, pos.z).roundToInt()} m"

        val fontRenderer = mc.fontRenderer
        var longestLine = ""
        for (strLine in str.lines()) {
            if (strLine.length > longestLine.length) {
                longestLine = strLine
            }
        }
        val stringWidth = fontRenderer.getStringWidth(longestLine)
        val stringHeight = (fontRenderer.FONT_HEIGHT + 2) * (str.lines().size - 1)
        val vertexHelper = VertexHelper(GlStateUtils.useVbo())
        val pos1 = Vec2d(stringWidth * -0.5 - 4.0, stringHeight * -0.5 - 4.0)
        val pos2 = Vec2d(stringWidth * 0.5 + 4.0, stringHeight * 0.5 + 2.0)

        /* Rectangle */
        RenderUtils2D.drawRectFilled(vertexHelper, pos1, pos2, ColorHolder(32, 32, 32, 172))

        /* Outline of the rectangle */
        RenderUtils2D.drawRectOutline(vertexHelper, pos1, pos2, 2f, ColorHolder(80, 80, 80, 232))

        glTranslated(0.0, -stringHeight * 0.5, 0.0)

        /* Draw string line by line */
        glEnable(GL_TEXTURE_2D)
        for (line in str.lines()) {
            val strLine = line.replace("${'\n'}", "")
            if (strLine.isBlank()) continue
            val strLineWidth = fontRenderer.getStringWidth(strLine).toFloat()
            fontRenderer.drawString(strLine, (strLineWidth / -2f), 0f, 0xffffff, false)
            glTranslated(0.0, fontRenderer.FONT_HEIGHT + 2.0, 0.0)
        }

        glPopMatrix()
    }

    override fun onUpdate() {
        ticks++
        if (ticks >= 20) {
            updateList()
            ticks = 0
        }
    }

    override fun onEnable() {
        updateList()
    }

    override fun onDisable() {
        currentServer = null
    }

    init {
        dimension.settingListener = Setting.SettingListeners {
            updateList()
        }
    }

    @EventHandler
    private val createWaypoint = Listener(EventHook { event: WaypointUpdateEvent.Create ->
        updateList()
    })

    @EventHandler
    private val removeWaypoint = Listener(EventHook { event: WaypointUpdateEvent.Remove ->
        updateList()
    })

    @EventHandler
    private val disconnectListener = Listener(EventHook { event: ConnectionEvent.Disconnect ->
        currentServer = null
    })

    private fun updateList() {
        if (currentServer == null) {
            currentServer = Waypoint.genServer()
        }
        waypoints.clear()
        if (currentServer == null) return
        waypoints.addAll(
                FileInstanceManager.waypoints.filter { w ->
                    (w.server == null || w.server == currentServer) && (dimension.value == Dimension.ANY || w.dimension == Waypoint.genDimension())
                }
        )
    }
}