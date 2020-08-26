package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.manager.mangers.FileInstanceManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.EntityUtils
import me.zeroeightsix.kami.util.WaypointInfo
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.graphics.GeometryMasks
import me.zeroeightsix.kami.util.graphics.KamiTessellator
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import org.lwjgl.opengl.GL11.*
import kotlin.math.max
import kotlin.math.pow
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
    private val page = register(Settings.e<Page>("Page", Page.INFOBOX))

    /* Page one */
    private val showName = register(Settings.booleanBuilder("ShowName").withValue(true).withVisibility { page.value == Page.INFOBOX }.build())
    private val showDate = register(Settings.booleanBuilder("ShowDate").withValue(true).withVisibility { page.value == Page.INFOBOX }.build())
    private val showCoords = register(Settings.booleanBuilder("ShowCoords").withValue(true).withVisibility { page.value == Page.INFOBOX }.build())
    private val showDist = register(Settings.booleanBuilder("ShowDistance").withValue(true).withVisibility { page.value == Page.INFOBOX }.build())
    private val textScale = register(Settings.floatBuilder("TextScale").withValue(1.0f).withRange(0.0f, 5.0f).withVisibility { page.value == Page.INFOBOX }.build())
    private val infoBoxRange = register(Settings.integerBuilder("InfoBoxRange").withValue(512).withRange(128, 2048).withVisibility { page.value == Page.INFOBOX }.build())

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

    private enum class Page {
        INFOBOX, ESP
    }

    private val waypoints = ArrayList<WaypointInfo>()

    override fun onWorldRender(event: RenderEvent) {
        if (mc.player == null || mc.renderManager.options == null || waypoints.isEmpty()) return
        val colour = ColorHolder(r.value, g.value, b.value)
        val renderer = ESPRenderer()
        renderer.aFilled = if (filled.value) aFilled.value else 0
        renderer.aOutline = if (outline.value) aOutline.value else 0
        renderer.aTracer = if (tracer.value) aTracer.value else 0
        renderer.thickness = thickness.value
        val glList = glGenLists(1)
        glNewList(glList, GL_COMPILE)
        for (waypoint in waypoints) {
            val pos = BlockPos(waypoint.pos.x, waypoint.pos.y, waypoint.pos.z)
            val distance = sqrt(mc.player.getDistanceSq(pos))
            /* Draw waypoint ESP */
            if (espRangeLimit.value && distance <= espRange.value) {
                renderer.add(AxisAlignedBB(pos), colour) /* Adds pos to ESPRenderer list */
                drawVerticalLines(pos, colour, aOutline.value) /* Draw lines from y 0 to y 256 */
            }
            /* Draw waypoint info box */
            if ((showCoords.value || showName.value || showDate.value || showDist.value) && distance <= infoBoxRange.value) {
                drawText(waypoint)
            }
        }
        glEndList()
        renderer.render(true)
        GlStateManager.disableDepth()
        glCallList(glList) /* Render the text after so it will be on top of the ESP */
    }

    private fun drawVerticalLines(pos: BlockPos, color: ColorHolder, a: Int) {
        val box = AxisAlignedBB(pos.x.toDouble(), 0.0, pos.z.toDouble(),
                pos.x + 1.0, 256.0, pos.z + 1.0)
        KamiTessellator.begin(GL_LINES)
        KamiTessellator.drawOutline(box, color, a, GeometryMasks.Quad.ALL, thickness.value)
        KamiTessellator.render()
    }

    private fun drawText(waypoint: WaypointInfo) {
        GlStateManager.pushMatrix()

        val x = (waypoint.pos.x + 0.5)
        val y = (waypoint.pos.y + 0.5)
        val z = (waypoint.pos.z + 0.5)
        GlStateManager.translate(x - mc.renderManager.renderPosX, y - mc.renderManager.renderPosY, z - mc.renderManager.renderPosZ)

        val viewerYaw = -mc.renderManager.playerViewY
        var viewerPitch = mc.renderManager.playerViewX
        if (mc.renderManager.options.thirdPersonView == 2) viewerPitch *= -1
        GlStateManager.rotate(viewerYaw, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(viewerPitch, 1.0f, 0.0f, 0.0f)

        val distance = sqrt(EntityUtils.getInterpolatedPos(mc.player, KamiTessellator.pTicks()).squareDistanceTo(x, y, z))
        val scale = max(distance, 2.0) / 8f * 1.2589254.pow(textScale.value.toDouble())
        GlStateManager.scale(scale, scale, scale)
        GlStateManager.scale(-0.025f, -0.025f, 0.025f)

        var str = ""
        if (showName.value) str += "${'\n'}${waypoint.name}"
        if (showDate.value) str += "${'\n'}${waypoint.date}"
        if (showCoords.value) str += "${'\n'}${waypoint.asString()}"
        if (showDist.value) str += "${'\n'}${distance.toInt()} m"

        val fontRenderer = mc.fontRenderer
        var longestLine = ""
        for (strLine in str.lines()) {
            if (strLine.length > longestLine.length) {
                longestLine = strLine
            }
        }
        val stringWidth = fontRenderer.getStringWidth(longestLine) + 8.0
        val stringHeight = (fontRenderer.FONT_HEIGHT + 1) * (str.lines().size - 1) + 5.0

        /* Rectangle */
        GlStateManager.color(0.1f, 0.1f, 0.1f, 0.7f)
        GlStateManager.glBegin(GL_QUADS) /* Was going to use VBO, don't know why it broke */
        glVertex3d(stringWidth * -0.5, 0.0, 0.0)
        glVertex3d(stringWidth * -0.5, -stringHeight, 0.0)
        glVertex3d(stringWidth * 0.5, -stringHeight, 0.0)
        glVertex3d(stringWidth * 0.5, 0.0, 0.0)
        GlStateManager.glEnd()

        /* Outline of the rectangle */
        GlStateManager.color(0.3f, 0.3f, 0.3f, 0.8f)
        GlStateManager.glLineWidth(2f)
        GlStateManager.glBegin(GL_LINE_LOOP)
        glVertex3d(stringWidth * -0.5, 0.0, 0.0)
        glVertex3d(stringWidth * -0.5, -stringHeight, 0.0)
        glVertex3d(stringWidth * 0.5, -stringHeight, 0.0)
        glVertex3d(stringWidth * 0.5, 0.0, 0.0)
        GlStateManager.glEnd()

        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.glNormal3f(0.0f, 1.0f, 0.0f)
        GlStateManager.translate(0.0, -stringHeight - 6.0, 0.0)

        /* Draw string line by line */
        for (line in str.lines()) {
            val strLine = line.replace("${'\n'}", "")
            if (strLine.isBlank()) continue
            val strLineWidth = fontRenderer.getStringWidth(strLine).toFloat()
            fontRenderer.drawString(strLine, (strLineWidth / -2f), 10f, 0xffffff, false)
            GlStateManager.translate(0.0, 10.0, 0.0)
        }

        GlStateManager.scale(-10f, -10f, 10f)
        GlStateManager.glNormal3f(0.0f, 0.0f, 0.0f)
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.popMatrix()
    }

    override fun onUpdate() {
        waypoints.clear()
        waypoints.addAll(FileInstanceManager.waypoints)
    }
}