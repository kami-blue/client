package me.zeroeightsix.kami.gui.kami.theme.staticui

import me.zeroeightsix.kami.gui.kami.component.Radar
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer
import me.zeroeightsix.kami.util.EntityUtils.isCurrentlyNeutral
import me.zeroeightsix.kami.util.EntityUtils.isPassiveMob
import me.zeroeightsix.kami.util.Friends
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.colourUtils.ColourHolder
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.RenderUtils2D.drawCircleFilled
import me.zeroeightsix.kami.util.graphics.RenderUtils2D.drawCircleOutline
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.math.Vec2d
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Created by 086 on 11/08/2017.
 */
class RadarUI : AbstractComponentUI<Radar?>() {
    var scale = 2f

    override fun handleSizeComponent(component: Radar?) {
        component!!
        component.width = radius * 2
        component.height = radius * 2
    }

    override fun renderComponent(component: Radar?, fontRenderer: FontRenderer?) {
        component!!
        scale = 2f
        GL11.glTranslated(component.width / 2.0, component.height / 2.0, 0.0)
        GlStateManager.pushMatrix()

        val vertexHelper = VertexHelper(GlStateUtils.useVbo())
        drawCircleFilled(vertexHelper, radius = radius.toDouble(), color = ColourHolder(28, 28, 28, 153))
        GL11.glRotatef(Wrapper.getPlayer().rotationYaw + 180, 0f, 0f, -1f)
        for (e in Wrapper.getWorld().loadedEntityList) {
            if (e == null || e.isDead) continue
            var red = 255
            var green = 255
            var blue = 255
            if (isPassiveMob(e) || Friends.isFriend(e.name)) { // green
                red = 0
                blue = 0
            } else if (isCurrentlyNeutral(e)) { // blue
                green = 0
                red = 0
            } else { // red
                blue = 0
                green = 0
            }
            val dX = e.posX - Wrapper.getPlayer().posX
            val dZ = e.posZ - Wrapper.getPlayer().posZ
            val distance = sqrt(dX.pow(2) + dZ.pow(2))
            if (distance > radius * scale || abs(Wrapper.getPlayer().posY - e.posY) > 30) continue

            drawCircleFilled(vertexHelper, Vec2d(dX / scale, dZ / scale), 2.5 / scale, color = ColourHolder(red, green, blue, 127))
        }


        drawCircleFilled(vertexHelper, radius = 3.0 / scale, color = ColourHolder(255, 255, 255, 255))

        drawCircleOutline(vertexHelper, radius = radius.toDouble(), lineWidth = 1.8f, color = ColourHolder(155, 144, 255, 255))

        component.theme.fontRenderer.drawString(-component.theme.fontRenderer.getStringWidth("+z") / 2, radius - component.theme.fontRenderer.fontHeight, "\u00A77z+")
        GL11.glRotatef(90f, 0f, 0f, 1f)
        component.theme.fontRenderer.drawString(-component.theme.fontRenderer.getStringWidth("+x") / 2, radius - component.theme.fontRenderer.fontHeight, "\u00A77x-")
        GL11.glRotatef(90f, 0f, 0f, 1f)
        component.theme.fontRenderer.drawString(-component.theme.fontRenderer.getStringWidth("-z") / 2, radius - component.theme.fontRenderer.fontHeight, "\u00A77z-")
        GL11.glRotatef(90f, 0f, 0f, 1f)
        component.theme.fontRenderer.drawString(-component.theme.fontRenderer.getStringWidth("+x") / 2, radius - component.theme.fontRenderer.fontHeight, "\u00A77x+")
        GlStateManager.popMatrix()

        GL11.glTranslated(-component.width / 2.0, -component.height / 2.0, 0.0)
    }

    companion object {
        const val radius = 45
    }
}