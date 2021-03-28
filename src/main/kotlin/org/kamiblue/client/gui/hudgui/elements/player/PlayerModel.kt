package org.kamiblue.client.gui.hudgui.elements.player

import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.math.MathHelper
import org.kamiblue.client.gui.hudgui.HudElement
import org.kamiblue.client.util.graphics.GlStateUtils
import org.kamiblue.client.util.graphics.KamiTessellator
import org.kamiblue.client.util.graphics.VertexHelper
import org.kamiblue.client.util.threads.runSafe
import org.lwjgl.opengl.GL11.*

internal object PlayerModel : HudElement(
    name = "PlayerModel",
    category = Category.PLAYER,
    description = "Your player icon, or players you attacked"
) {
    private val resetDelay by setting("Reset Delay", 100, 0..200, 5)
    private val emulatePitch by setting("Emulate Pitch", true)
    private val emulateYaw by setting("Emulate Yaw", false)

    override val hudWidth: Float get() = 50.0f
    override val hudHeight: Float get() = 80.0f

    override val resizable: Boolean = true

    override fun renderHud(vertexHelper: VertexHelper) {
        if (mc.renderManager.renderViewEntity == null) return

        super.renderHud(vertexHelper)
        runSafe {
            val attackedEntity: EntityLivingBase? = player.lastAttackedEntity
            val entity = if (attackedEntity != null && player.ticksExisted - player.lastAttackedEntityTime <= resetDelay) {
                attackedEntity
            } else {
                player
            }

            val yaw = if (emulateYaw) interpolateAndWrap(entity.prevRotationYaw, entity.rotationYaw) else 0.0f
            val pitch = if (emulatePitch) interpolateAndWrap(entity.prevRotationPitch, entity.rotationPitch) else 0.0f

            glPushMatrix()
            glTranslatef(renderWidth / scale / 2.0f, renderHeight / scale - 8.0f, 0.0f)
            GlStateUtils.depth(true)
            glColor4f(1.0f, 1.0f, 1.0f, 1.0f)

            GuiInventory.drawEntityOnScreen(0, 0, 35, -yaw, -pitch, entity)

            glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
            GlStateUtils.depth(false)
            GlStateUtils.texture2d(true)
            GlStateUtils.blend(true)
            GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

            GlStateManager.disableColorMaterial()
            glPopMatrix()
        }
    }

    private fun interpolateAndWrap(prev: Float, current: Float): Float {
        return MathHelper.wrapDegrees(prev + (current - prev) * KamiTessellator.pTicks())
    }
}