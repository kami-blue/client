package me.zeroeightsix.kami.gui.kami.theme.staticui

import me.zeroeightsix.kami.gui.kami.component.Radar
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI
import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.module.modules.render.NewChunks
import me.zeroeightsix.kami.util.EntityUtils.isCurrentlyNeutral
import me.zeroeightsix.kami.util.EntityUtils.isPassiveMob
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.RenderUtils2D.drawCircleFilled
import me.zeroeightsix.kami.util.graphics.RenderUtils2D.drawCircleOutline
import me.zeroeightsix.kami.util.graphics.RenderUtils2D.drawRectFilled
import me.zeroeightsix.kami.util.graphics.RenderUtils2D.drawRectOutline
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter
import me.zeroeightsix.kami.util.math.Vec2d
import net.minecraft.entity.Entity
import org.lwjgl.opengl.GL11.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Created by 086 on 11/08/2017.
 */
class RadarUI : AbstractComponentUI<Radar?>() {
    private val filledCircleColor = ColorHolder(28, 28, 28, 200)
    private val outlineCircleColor = ColorHolder(155, 144, 255, 255)
    private val filledCircleColorDarker = ColorHolder(255, 255, 255, 224)
    private val rectFilledColor = ColorHolder(100, 100, 100, 100)
    private val rectOutlineColor = ColorHolder(255, 0, 0, 100)
    private val rectFilledColorOther = ColorHolder(255, 0, 0, 100)
    private val radius = 45.0f

    override fun handleSizeComponent(component: Radar?) {
        component!!

        component.width = (radius * 2f).roundToInt()
        component.height = (radius * 2f).roundToInt()
    }

    override fun renderComponent(component: Radar?) {
        Wrapper.player ?: return
        Wrapper.world ?: return
        component!!

        glPushMatrix()
        glTranslated(component.width / 2.0, component.height / 2.0, 0.0)

        val vertexHelper = VertexHelper(GlStateUtils.useVbo())
        drawCircleFilled(vertexHelper, radius = radius.toDouble(), color = filledCircleColor)
        drawCircleOutline(vertexHelper, radius = radius.toDouble(), lineWidth = 1.8f, color = outlineCircleColor)
        glRotatef(Wrapper.player!!.rotationYaw + 180, 0f, 0f, -1f)

        if (NewChunks.isEnabled && NewChunks.isRadarMode) renderNewChunks(vertexHelper)

        for (entity in Wrapper.world!!.loadedEntityList) {
            if (entity == null || entity.isDead || entity == Wrapper.player) continue
            val dX = entity.posX - Wrapper.player!!.posX
            val dZ = entity.posZ - Wrapper.player!!.posZ
            val distance = sqrt(dX.pow(2) + dZ.pow(2))
            if (distance > radius * NewChunks.radarScale.value || abs(Wrapper.player!!.posY - entity.posY) > 30) continue
            val color = getColor(entity)

            drawCircleFilled(vertexHelper, Vec2d(dX / NewChunks.radarScale.value, dZ / NewChunks.radarScale.value), 2.5 / NewChunks.radarScale.value, color = color)
        }

        drawCircleFilled(vertexHelper, radius = 1.0, color = filledCircleColorDarker)

        FontRenderAdapter.drawString("\u00A77z+", -FontRenderAdapter.getStringWidth("+z") / 2f, radius - FontRenderAdapter.getFontHeight(), drawShadow = true)
        glRotatef(90f, 0f, 0f, 1f)
        FontRenderAdapter.drawString("\u00A77x-", -FontRenderAdapter.getStringWidth("+x") / 2f, radius - FontRenderAdapter.getFontHeight(), drawShadow = true)
        glRotatef(90f, 0f, 0f, 1f)
        FontRenderAdapter.drawString("\u00A77z-", -FontRenderAdapter.getStringWidth("-z") / 2f, radius - FontRenderAdapter.getFontHeight(), drawShadow = true)
        glRotatef(90f, 0f, 0f, 1f)
        FontRenderAdapter.drawString("\u00A77x+", -FontRenderAdapter.getStringWidth("+x") / 2f, radius - FontRenderAdapter.getFontHeight(), drawShadow = true)

        glPopMatrix()
    }

    private fun renderNewChunks(vertexHelper: VertexHelper) {
        val playerOffset = Vec2d((Wrapper.player!!.posX - (Wrapper.player!!.chunkCoordX shl 4)), (Wrapper.player!!.posZ - (Wrapper.player!!.chunkCoordZ shl 4)))
        val chunkDist = (radius * NewChunks.radarScale.value).toInt() shr 4
        for (chunkX in -chunkDist..chunkDist) {
            for (chunkZ in -chunkDist..chunkDist) {
                val pos0 = getChunkPos(chunkX, chunkZ, playerOffset)
                val pos1 = getChunkPos(chunkX + 1, chunkZ + 1, playerOffset)

                if (squareInRadius(pos0, pos1)) {
                    val chunk = Wrapper.world!!.getChunk(Wrapper.player!!.chunkCoordX + chunkX, Wrapper.player!!.chunkCoordZ + chunkZ)
                    if (!chunk.isLoaded)
                        drawRectFilled(vertexHelper, pos0, pos1, rectFilledColor)
                    drawRectOutline(vertexHelper, pos0, pos1, 0.3f, rectOutlineColor)
                }
            }
        }

        for (chunk in NewChunks.chunks) {

            val pos0 = getChunkPos(chunk.x - Wrapper.player!!.chunkCoordX, chunk.z - Wrapper.player!!.chunkCoordZ, playerOffset)
            val pos1 = getChunkPos(chunk.x - Wrapper.player!!.chunkCoordX + 1, chunk.z - Wrapper.player!!.chunkCoordZ + 1, playerOffset)

            if (squareInRadius(pos0, pos1)) {
                drawRectFilled(vertexHelper, pos0, pos1, rectFilledColorOther)
            }
        }
    }

    private fun getColor(entity: Entity): ColorHolder {
        return if (isPassiveMob(entity) || FriendManager.isFriend(entity.name)) { // green
            ColorHolder(32, 224, 32, 224)
        } else if (isCurrentlyNeutral(entity)) { // yellow
            ColorHolder(255, 240, 32)
        } else { // red
            ColorHolder(255, 32, 32)
        }
    }

    // p2.x > p1.x and p2.y > p1.y is assumed
    private fun squareInRadius(p1: Vec2d, p2: Vec2d): Boolean {
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
        return Vec2d((x shl 4).toDouble(), (z shl 4).toDouble()).subtract(playerOffset).divide(NewChunks.radarScale.value)
    }
}