package me.zeroeightsix.kami.gui.kami.theme.staticui

import me.zeroeightsix.kami.gui.kami.component.Radar
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer
import me.zeroeightsix.kami.util.EntityUtils.isCurrentlyNeutral
import me.zeroeightsix.kami.util.EntityUtils.isPassiveMob
import me.zeroeightsix.kami.util.Friends
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.RenderUtils2D.drawCircleFilled
import me.zeroeightsix.kami.util.graphics.RenderUtils2D.drawCircleOutline
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.math.Vec2d
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import org.lwjgl.opengl.GL11.glRotatef
import org.lwjgl.opengl.GL11.glTranslated
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Created by 086 on 11/08/2017.
 */
class RadarUI : AbstractComponentUI<Radar?>() {

    override fun handleSizeComponent(component: Radar?) {
        component!!

        component.width = radiusInt * 2
        component.height = radiusInt * 2
    }

    override fun renderComponent(component: Radar?, fontRenderer: FontRenderer?) {
        Wrapper.player ?: return
        Wrapper.world ?: return
        component!!

        GlStateManager.pushMatrix()
        glTranslated(component.width / 2.0, component.height / 2.0, 0.0)

        val vertexHelper = VertexHelper(GlStateUtils.useVbo())
        drawCircleFilled(vertexHelper, radius = radius, color = ColorHolder(28, 28, 28, 200))
        drawCircleOutline(vertexHelper, radius = radius, lineWidth = 1.8f, color = ColorHolder(155, 144, 255, 255))
        drawCircleFilled(vertexHelper, radius = 2.0 / scale, color = ColorHolder(255, 255, 255, 224))

        glRotatef(Wrapper.player!!.rotationYaw + 180, 0f, 0f, -1f)
        for (entity in Wrapper.world!!.loadedEntityList) {
            if (entity == null || entity.isDead || entity == Wrapper.player) continue
            val dX = entity.posX - Wrapper.player!!.posX
            val dZ = entity.posZ - Wrapper.player!!.posZ
            val distance = sqrt(dX.pow(2) + dZ.pow(2))
            if (distance > radius * scale || abs(Wrapper.player!!.posY - entity.posY) > 30) continue
            val color = getColor(entity)

            drawCircleFilled(vertexHelper, Vec2d(dX / scale, dZ / scale), 2.5 / scale, color = color)
        }

        component.theme.fontRenderer.drawString(-component.theme.fontRenderer.getStringWidth("+z") / 2, radiusInt - component.theme.fontRenderer.fontHeight, "\u00A77z+")
        glRotatef(90f, 0f, 0f, 1f)
        component.theme.fontRenderer.drawString(-component.theme.fontRenderer.getStringWidth("+x") / 2, radiusInt - component.theme.fontRenderer.fontHeight, "\u00A77x-")
        glRotatef(90f, 0f, 0f, 1f)
        component.theme.fontRenderer.drawString(-component.theme.fontRenderer.getStringWidth("-z") / 2, radiusInt - component.theme.fontRenderer.fontHeight, "\u00A77z-")
        glRotatef(90f, 0f, 0f, 1f)
        component.theme.fontRenderer.drawString(-component.theme.fontRenderer.getStringWidth("+x") / 2, radiusInt - component.theme.fontRenderer.fontHeight, "\u00A77x+")

        GlStateManager.popMatrix()
    }

    private fun getColor(entity: Entity): ColorHolder {
        return if (isPassiveMob(entity) || Friends.isFriend(entity.name)) { // green
            ColorHolder(32, 224, 32, 224)
        } else if (isCurrentlyNeutral(entity)) { // yellow
            ColorHolder(255, 240, 32)
        } else { // red
            ColorHolder(255, 32, 32)
        }
    }

    companion object {
        const val scale = 2.0
        const val radius = 45.0
        const val radiusInt = 45
    }
}