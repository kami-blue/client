package org.kamiblue.client.gui.hudgui.elements.world

import net.minecraft.entity.EntityLivingBase
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.gui.hudgui.HudElement
import org.kamiblue.client.manager.managers.FriendManager
import org.kamiblue.client.module.modules.client.GuiColors
import org.kamiblue.client.module.modules.render.NewChunks
import org.kamiblue.client.setting.GuiConfig.setting
import org.kamiblue.client.util.EntityUtils
import org.kamiblue.client.util.EntityUtils.isNeutral
import org.kamiblue.client.util.EntityUtils.isPassive
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.graphics.RenderUtils2D.drawCircleFilled
import org.kamiblue.client.util.graphics.RenderUtils2D.drawCircleOutline
import org.kamiblue.client.util.graphics.RenderUtils2D.drawRectFilled
import org.kamiblue.client.util.graphics.RenderUtils2D.drawRectOutline
import org.kamiblue.client.util.graphics.VertexHelper
import org.kamiblue.client.util.graphics.font.FontRenderAdapter
import org.kamiblue.client.util.math.Vec2d
import org.kamiblue.client.util.threads.runSafe
import org.lwjgl.opengl.GL11.glRotatef
import org.lwjgl.opengl.GL11.glTranslated
import kotlin.math.abs
import kotlin.math.min

object Radar : HudElement(
    name = "Radar",
    category = Category.WORLD,
    description = "Shows entities and new chunks"
) {

    private val radarScale by setting("Radar scale", 3f, 1f..10f, 0.1f)

    /* Entity type settings */
    private val players = setting("Players", true)
    private val passive = setting("Passive Mobs", false)
    private val neutral = setting("Neutral Mobs", true)
    private val hostile = setting("Hostile Mobs", true)
    private val invisible = setting("Invisible Entities", true)

    private val chunkGridColor by setting("Chunk grid color", ColorHolder(255, 0, 0, 100), true)
    private val distantChunkColor by setting("Distant chunk color", ColorHolder(100, 100, 100, 100), true, description = "Chunks that are not in render distance")
    private val newChunkColor by setting("New chunk color", ColorHolder(255, 0, 0, 100), true)

    override val hudWidth: Float = 130.0f
    override val hudHeight: Float = 130.0f

    private val radius get() = min(hudWidth, hudHeight) / 2

    override fun renderHud(vertexHelper: VertexHelper) {
        super.renderHud(vertexHelper)
        runSafe {
            drawBorder(vertexHelper)
            if (NewChunks.isEnabled && NewChunks.renderMode.value != NewChunks.RenderMode.WORLD) drawNewChunks(vertexHelper)
            drawEntities(vertexHelper)
            drawLabels()
        }
    }

    private fun SafeClientEvent.drawBorder(vertexHelper: VertexHelper) {
        glTranslated(radius.toDouble(), radius.toDouble(), 0.0)
        drawCircleFilled(vertexHelper, radius = radius.toDouble(), color = GuiColors.backGround)
        drawCircleOutline(vertexHelper, radius = radius.toDouble(), lineWidth = 1.8f, color = primaryColor)
        glRotatef(player.rotationYaw + 180, 0f, 0f, -1f)
    }

    private fun SafeClientEvent.drawNewChunks(vertexHelper: VertexHelper) {
        val playerOffset = Vec2d((player.posX - (player.chunkCoordX shl 4)), (player.posZ - (player.chunkCoordZ shl 4)))
        val chunkDist = (radius * radarScale).toInt() shr 4
        for (chunkX in -chunkDist..chunkDist) {
            for (chunkZ in -chunkDist..chunkDist) {
                val pos0 = getChunkPos(chunkX, chunkZ, playerOffset)
                val pos1 = getChunkPos(chunkX + 1, chunkZ + 1, playerOffset)

                if (isSquareInRadius(pos0, pos1)) {
                    val chunk = world.getChunk(player.chunkCoordX + chunkX, player.chunkCoordZ + chunkZ)
                    if (!chunk.isLoaded)
                        drawRectFilled(vertexHelper, pos0, pos1, distantChunkColor)
                    drawRectOutline(vertexHelper, pos0, pos1, 0.3f, chunkGridColor)
                }
            }
        }

        for (chunk in NewChunks.chunks) {
            val pos0 = getChunkPos(chunk.x - player.chunkCoordX, chunk.z - player.chunkCoordZ, playerOffset)
            val pos1 = getChunkPos(chunk.x - player.chunkCoordX + 1, chunk.z - player.chunkCoordZ + 1, playerOffset)

            if (isSquareInRadius(pos0, pos1)) {
                drawRectFilled(vertexHelper, pos0, pos1, newChunkColor)
            }
        }
    }

    private fun SafeClientEvent.drawEntities(vertexHelper: VertexHelper) {
        drawCircleFilled(vertexHelper, radius = 1.0, color = primaryColor) //player marker

        val playerTargets = arrayOf(players.value, true, true) //enable friends and sleeping
        val mobTargets = arrayOf(true, passive.value, neutral.value, hostile.value) //enable mobs
        for (entity in EntityUtils.getTargetList(playerTargets, mobTargets, invisible.value, radius * radarScale, ignoreSelf = true)) {
            val entityPosDelta = entity.position.subtract(player.position)
            if (abs(entityPosDelta.y) > 30) continue
            drawCircleFilled(vertexHelper, Vec2d(entityPosDelta.x.toDouble(), entityPosDelta.z.toDouble()).div(radarScale.toDouble()), 2.5 / radarScale, color = getColor(entity))
        }
    }

    private fun drawLabels() {
        FontRenderAdapter.drawString("Z+", -FontRenderAdapter.getStringWidth("+z") / 2f, radius - FontRenderAdapter.getFontHeight(), drawShadow = true, color = secondaryColor)
        glRotatef(90f, 0f, 0f, 1f)
        FontRenderAdapter.drawString("X-", -FontRenderAdapter.getStringWidth("+x") / 2f, radius - FontRenderAdapter.getFontHeight(), drawShadow = true, color = secondaryColor)
        glRotatef(90f, 0f, 0f, 1f)
        FontRenderAdapter.drawString("Z-", -FontRenderAdapter.getStringWidth("-z") / 2f, radius - FontRenderAdapter.getFontHeight(), drawShadow = true, color = secondaryColor)
        glRotatef(90f, 0f, 0f, 1f)
        FontRenderAdapter.drawString("X+", -FontRenderAdapter.getStringWidth("+x") / 2f, radius - FontRenderAdapter.getFontHeight(), drawShadow = true, color = secondaryColor)
    }

    private fun getColor(entity: EntityLivingBase): ColorHolder {
        return if (entity.isPassive || FriendManager.isFriend(entity.name)) { // green
            ColorHolder(32, 224, 32, 224)
        } else if (entity.isNeutral) { // yellow
            ColorHolder(255, 240, 32)
        } else { // red
            ColorHolder(255, 32, 32)
        }
    }

    // p2.x > p1.x and p2.y > p1.y is assumed
    private fun isSquareInRadius(p1: Vec2d, p2: Vec2d): Boolean {
        return if ((p1.x + p2.x) / 2 > 0) {
            if ((p1.y + p2.y) / 2 > 0) {
                p2.length()
            } else {
                Vec2d(p2.x, p1.y).length()
            }
        } else {
            if ((p1.y + p2.y) / 2 > 0) {
                Vec2d(p1.x, p2.y).length()
            } else {
                p1.length()
            }
        } < radius
    }

    private fun getChunkPos(x: Int, z: Int, playerOffset: Vec2d): Vec2d {
        return Vec2d((x shl 4).toDouble(), (z shl 4).toDouble()).minus(playerOffset).div(radarScale.toDouble())
    }
}