package org.kamiblue.client.gui.hudgui.elements.world

import net.minecraft.entity.EntityLivingBase
import org.kamiblue.client.event.KamiEventBus.post
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.RenderRadarEvent
import org.kamiblue.client.gui.hudgui.HudElement
import org.kamiblue.client.manager.managers.FriendManager
import org.kamiblue.client.module.modules.client.GuiColors
import org.kamiblue.client.util.EntityUtils
import org.kamiblue.client.util.EntityUtils.isNeutral
import org.kamiblue.client.util.EntityUtils.isPassive
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.graphics.RenderUtils2D.drawCircleFilled
import org.kamiblue.client.util.graphics.RenderUtils2D.drawCircleOutline
import org.kamiblue.client.util.graphics.VertexHelper
import org.kamiblue.client.util.graphics.font.FontRenderAdapter
import org.kamiblue.client.util.math.Vec2d
import org.kamiblue.client.util.threads.runSafe
import org.lwjgl.opengl.GL11.glRotatef
import org.lwjgl.opengl.GL11.glTranslated
import kotlin.math.abs
import kotlin.math.min

internal object Radar : HudElement(
    name = "Radar",
    category = Category.WORLD,
    description = "Shows entities and new chunks"
) {

    private val players = setting("Players", true)
    private val passive = setting("Passive Mobs", false)
    private val neutral = setting("Neutral Mobs", true)
    private val hostile = setting("Hostile Mobs", true)
    private val invisible = setting("Invisible Entities", true)

    override val hudWidth: Float = 130.0f
    override val hudHeight: Float = 130.0f

    private val radius get() = min(hudWidth, hudHeight) / 2

    override fun renderHud(vertexHelper: VertexHelper) {
        super.renderHud(vertexHelper)

        runSafe {
            drawBorder(vertexHelper)
            post(RenderRadarEvent(vertexHelper, radius, scale)) // Let other modules display radar elements
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

    private fun SafeClientEvent.drawEntities(vertexHelper: VertexHelper) {
        drawCircleFilled(vertexHelper, radius = 1.0, color = primaryColor) // player marker

        val playerTargets = arrayOf(players.value, true, true) // Enable friends and sleeping
        val mobTargets = arrayOf(true, passive.value, neutral.value, hostile.value) // Enable mobs

        for (entity in EntityUtils.getTargetList(playerTargets, mobTargets, invisible.value, radius * scale, ignoreSelf = true)) {
            val entityPosDelta = entity.position.subtract(player.position)
            if (abs(entityPosDelta.y) > 30) continue
            drawCircleFilled(vertexHelper, Vec2d(entityPosDelta.x.toDouble(), entityPosDelta.z.toDouble()).div(scale.toDouble()), 2.5 / scale, color = getColor(entity))
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

}