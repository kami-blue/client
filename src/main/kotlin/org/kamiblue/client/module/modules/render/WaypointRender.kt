package org.kamiblue.client.module.modules.render

import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.events.*
import org.kamiblue.client.manager.managers.WaypointManager
import org.kamiblue.client.manager.managers.WaypointManager.Waypoint
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.graphics.*
import org.kamiblue.client.util.graphics.font.HAlign
import org.kamiblue.client.util.graphics.font.TextComponent
import org.kamiblue.client.util.graphics.font.VAlign
import org.kamiblue.client.util.math.Vec2d
import org.kamiblue.client.util.math.VectorUtils.distanceTo
import org.kamiblue.client.util.math.VectorUtils.toVec3dCenter
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.event.listener.listener
import org.lwjgl.opengl.GL11.*
import java.util.*
import kotlin.math.roundToInt
import kotlin.math.sqrt

internal object WaypointRender : Module(
    name = "WaypointRender",
    description = "Render saved waypoints",
    category = Category.RENDER
) {

    private val renderMode by setting("Mode", RenderMode.BOTH)
    private val page by setting("Page", Page.INFO_BOX)

    /* Page one */
    private val dimension = setting("Dimension", Dimension.CURRENT, { page == Page.INFO_BOX })
    private val showName = setting("Show Name", true, { page == Page.INFO_BOX })
    private val showDate = setting("Show Date", false, { page == Page.INFO_BOX })
    private val showCoordinates = setting("Show Coordinates", true, { page == Page.INFO_BOX })
    private val showDist = setting("Show Distance", true, { page == Page.INFO_BOX })
    private val textScale by setting("Text Scale", 1.0f, 0.0f..2.0f, 0.1f, { page == Page.INFO_BOX })
    private val infoBoxRange by setting("Info Box Range", 512, 128..2048, 64, { page == Page.INFO_BOX })

    /* Page two */
    private val espRangeLimit by setting("Render Range", true, { page == Page.RENDER && renderMode != RenderMode.RADAR })
    private val espRange by setting("Range", 4096, 1024..16384, 1024, { page == Page.RENDER && espRangeLimit && renderMode != RenderMode.RADAR })
    private val filled by setting("Filled", true, { page == Page.RENDER && renderMode != RenderMode.RADAR })
    private val outline by setting("Outline", true, { page == Page.RENDER && renderMode != RenderMode.RADAR })
    private val tracer by setting("Tracer", true, { page == Page.RENDER && renderMode != RenderMode.RADAR })
    private val color by setting("Color", ColorHolder(31, 200, 63), false, { page == Page.RENDER })
    private val aFilled by setting("Filled Alpha", 63, 0..255, 1, { page == Page.RENDER && filled && renderMode != RenderMode.RADAR })
    private val aOutline by setting("Outline Alpha", 160, 0..255, 1, { page == Page.RENDER && outline && renderMode != RenderMode.RADAR })
    private val aTracer by setting("Tracer Alpha", 200, 0..255, 1, { page == Page.RENDER && tracer && renderMode != RenderMode.RADAR })
    private val thickness by setting("Line Thickness", 2.0f, 0.25f..8.0f, 0.25f, { page == Page.RENDER })
    private val waypointDot by setting("Waypoint Dot Size", 3.5, 1.0..8.0, 0.5, { page == Page.RENDER && renderMode != RenderMode.WORLD })

    private enum class Dimension {
        CURRENT, ANY
    }

    private enum class RenderMode {
        WORLD, RADAR, BOTH
    }

    private enum class Page {
        INFO_BOX, RENDER
    }

    // This has to be sorted so the further ones doesn't overlaps the closer ones
    private val waypointMap = TreeMap<Waypoint, TextComponent>(compareByDescending {
        mc.player?.distanceTo(it.pos) ?: it.pos.getDistance(0, -69420, 0)
    })
    private var currentServer: String? = null
    private var timer = TickTimer(TimeUnit.SECONDS)
    private var prevDimension = -2
    private val lockObject = Any()

    init {
        listener<RenderWorldEvent> {
            if (waypointMap.isEmpty() || renderMode == RenderMode.RADAR) return@listener

            val renderer = ESPRenderer()
            renderer.aFilled = if (filled) aFilled else 0
            renderer.aOutline = if (outline) aOutline else 0
            renderer.aTracer = if (tracer) aTracer else 0
            renderer.thickness = thickness

            GlStateUtils.depth(false)

            for (waypoint in waypointMap.keys) {
                val distance = mc.player.distanceTo(waypoint.pos)
                if (espRangeLimit && distance > espRange) continue

                renderer.add(AxisAlignedBB(waypoint.pos), color) /* Adds pos to ESPRenderer list */
                drawVerticalLines(waypoint.pos, color, aOutline) /* Draw lines from y 0 to y 256 */
            }

            GlStateUtils.depth(true)
            renderer.render(true)
        }
    }

    private fun drawVerticalLines(pos: BlockPos, color: ColorHolder, a: Int) {
        val box = AxisAlignedBB(
            pos.x.toDouble(), 0.0, pos.z.toDouble(),
            pos.x + 1.0, 256.0, pos.z + 1.0
        )

        KamiTessellator.begin(GL_LINES)
        KamiTessellator.drawOutline(box, color, a, GeometryMasks.Quad.ALL, thickness)
        KamiTessellator.render()
    }

    init {
        listener<RenderOverlayEvent> {
            if (waypointMap.isEmpty() || !showCoordinates.value && !showName.value && !showDate.value && !showDist.value) {
                return@listener
            }

            GlStateUtils.rescaleActual()

            for ((waypoint, textComponent) in waypointMap) {
                val distance = sqrt(mc.player.getDistanceSqToCenter(waypoint.pos))
                if (distance > infoBoxRange) continue

                drawText(waypoint.pos, textComponent, distance.roundToInt())
            }

            GlStateUtils.rescaleMc()
        }
    }

    private fun drawText(pos: BlockPos, textComponentIn: TextComponent, distance: Int) {
        glPushMatrix()

        val screenPos = ProjectionUtils.toScreenPos(pos.toVec3dCenter())
        glTranslatef(screenPos.x.toFloat(), screenPos.y.toFloat(), 0f)
        glScalef(textScale * 2f, textScale * 2f, 0f)

        val textComponent = TextComponent(textComponentIn).apply { if (showDist.value) add("$distance m") }
        val stringWidth = textComponent.getWidth()
        val stringHeight = textComponent.getHeight(2)
        val vertexHelper = VertexHelper(GlStateUtils.useVbo())
        val pos1 = Vec2d(stringWidth * -0.5 - 4.0, stringHeight * -0.5 - 4.0)
        val pos2 = Vec2d(stringWidth * 0.5 + 4.0, stringHeight * 0.5 + 4.0)

        RenderUtils2D.drawRectFilled(vertexHelper, pos1, pos2, ColorHolder(32, 32, 32, 172))
        RenderUtils2D.drawRectOutline(vertexHelper, pos1, pos2, 2f, ColorHolder(80, 80, 80, 232))
        textComponent.draw(drawShadow = false, horizontalAlign = HAlign.CENTER, verticalAlign = VAlign.CENTER)

        glPopMatrix()
    }

    init {
        onEnable {
            timer.reset(-10000L) // Update the map immediately and thread safely
        }

        onDisable {
            currentServer = null
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (WaypointManager.genDimension() != prevDimension || timer.tick(10L, false)) {
                updateList()
            }
        }

        listener<WaypointUpdateEvent> {
            // This could be called from another thread so we have to synchronize the map
            synchronized(lockObject) {
                when (it.type) {
                    WaypointUpdateEvent.Type.ADD -> updateTextComponent(it.waypoint)
                    WaypointUpdateEvent.Type.REMOVE -> waypointMap.remove(it.waypoint)
                    WaypointUpdateEvent.Type.CLEAR -> waypointMap.clear()
                    WaypointUpdateEvent.Type.RELOAD -> updateList()
                    else -> {
                        // this is fine, Java meme
                    }
                }
            }
        }

        safeListener<RenderRadarEvent> {
            if (renderMode == RenderMode.WORLD) return@safeListener

            for (waypoint in waypointMap.keys) {
                val pos = getPos(waypoint, Vec2d(player.position.x.toDouble(), player.position.z.toDouble()), it.scale)

                // Check if pos is inside radius
                if (pos.length() < it.radius) {
                    RenderUtils2D.drawCircleFilled(it.vertexHelper, pos, waypointDot / it.scale, color = color)
                }
            }
        }

        listener<ConnectionEvent.Disconnect> {
            currentServer = null
        }
    }

    private fun getPos(waypoint: Waypoint, playerOffset: Vec2d, scale: Float): Vec2d {
        val position = waypoint.pos
        return Vec2d(position.x.toDouble(), position.z.toDouble()).minus(playerOffset).div(scale.toDouble())
    }

    private fun updateList() {
        timer.reset()
        prevDimension = WaypointManager.genDimension()
        if (currentServer == null) {
            currentServer = WaypointManager.genServer()
        }

        val cacheList = WaypointManager.waypoints.filter {
            (it.server == null || it.server == currentServer)
                && (dimension.value == Dimension.ANY || it.dimension == prevDimension)
        }
        waypointMap.clear()

        for (waypoint in cacheList) updateTextComponent(waypoint)
    }

    private fun updateTextComponent(waypoint: Waypoint?) {
        if (waypoint == null) return

        // Don't wanna update this continuously
        waypointMap.computeIfAbsent(waypoint) {
            TextComponent().apply {
                if (showName.value) addLine(waypoint.name)
                if (showDate.value) addLine(waypoint.date)
                if (showCoordinates.value) addLine(waypoint.toString())
            }
        }
    }

    init {
        with(
            { synchronized(lockObject) { updateList() } } // This could be called from another thread so we have to synchronize the map
        ) {
            showName.listeners.add(this)
            showDate.listeners.add(this)
            showCoordinates.listeners.add(this)
            showDist.listeners.add(this)
        }

        dimension.listeners.add {
            synchronized(lockObject) {
                waypointMap.clear(); updateList()
            }
        }
    }
}