package me.zeroeightsix.kami.gui.hudgui.elements.player

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.graphics.KamiTessellator
import me.zeroeightsix.kami.util.graphics.VertexHelper
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL11

object PlayerModel : HudElement(
    name = "PlayerModel",
    category = Category.PLAYER,
    description = "Your player icon, or players you attacked"
) {
    private val scale = setting("Size", 100, 10..200, 2)
    private val resetDelay = setting("ResetDelay", 100, 0..200, 10)
    private val emulatePitch = setting("EmulatePitch", true)
    private val emulateYaw = setting("EmulateYaw", false)

    override val minWidth: Float get() = adjust(114f)
    override val minHeight: Float get() = adjust(204f)
    override val resizable: Boolean = true

    override fun renderHud(vertexHelper: VertexHelper) {
        if (mc.player == null || mc.renderManager.renderViewEntity == null) return
        super.renderHud(vertexHelper)

        val attackedEntity = mc.player?.lastAttackedEntity
        val entity = if (attackedEntity != null && mc.player.ticksExisted - mc.player.lastAttackedEntityTime <= resetDelay.value) {
            attackedEntity
        } else {
            mc.player
        }

        val yaw = if (emulateYaw.value) interpolateAndWrap(entity.prevRotationYaw, entity.rotationYaw) else 0.0f
        val pitch = if (emulatePitch.value) interpolateAndWrap(entity.prevRotationPitch, entity.rotationPitch) else 0.0f

        GL11.glTranslatef(width / 2, height - adjust(7.5f), 0f)
        GuiInventory.drawEntityOnScreen(0, 0, scale.value, -yaw, -pitch, entity)
    }

    private fun interpolateAndWrap(prev: Float, current: Float): Float {
        return MathHelper.wrapDegrees(prev + (current - prev) * KamiTessellator.pTicks())
    }

    private fun adjust(value: Float) = (value * scale.value) / 100f
}