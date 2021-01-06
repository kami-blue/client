package me.zeroeightsix.kami.gui.hudgui.elements.player

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.KamiTessellator
import me.zeroeightsix.kami.util.graphics.VertexHelper
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL11.*
import kotlin.math.roundToInt

object PlayerModel : HudElement(
    name = "PlayerModel",
    category = Category.PLAYER,
    description = "Your player icon, or players you attacked"
) {
    private val scale by setting("Size", 1.0f, 0.5f..2.0f, 0.05f)
    private val resetDelay by setting("ResetDelay", 100, 0..200, 5)
    private val emulatePitch by setting("EmulatePitch", true)
    private val emulateYaw by setting("EmulateYaw", false)

    override val minWidth: Float get() = adjust(114.0f)
    override val minHeight: Float get() = adjust(204.0f)
    override val resizable: Boolean = true

    override fun renderHud(vertexHelper: VertexHelper) {
        if (mc.player == null || mc.renderManager.renderViewEntity == null) return
        super.renderHud(vertexHelper)

        val attackedEntity = mc.player?.lastAttackedEntity
        val entity = if (attackedEntity != null && mc.player.ticksExisted - mc.player.lastAttackedEntityTime <= resetDelay) {
            attackedEntity
        } else {
            mc.player
        }

        val yaw = if (emulateYaw) interpolateAndWrap(entity.prevRotationYaw, entity.rotationYaw) else 0.0f
        val pitch = if (emulatePitch) interpolateAndWrap(entity.prevRotationPitch, entity.rotationPitch) else 0.0f

        glPushMatrix()
        glTranslatef(width / 2, height - adjust(7.5f), 0f)
        GlStateUtils.depth(true)
        GuiInventory.drawEntityOnScreen(0, 0, (scale * 35.0f).roundToInt(), -yaw, -pitch, entity)
        GlStateUtils.depth(false)
        GlStateUtils.texture2d(true)
        GlStateUtils.blend(true)
        GlStateManager.disableColorMaterial()
        glPopMatrix()
    }

    private fun interpolateAndWrap(prev: Float, current: Float): Float {
        return MathHelper.wrapDegrees(prev + (current - prev) * KamiTessellator.pTicks())
    }

    private fun adjust(value: Float) = value * scale * 0.35f
}