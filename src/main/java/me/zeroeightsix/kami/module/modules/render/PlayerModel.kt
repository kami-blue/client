package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderOverlayEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.KamiTessellator
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.math.MathHelper

@Module.Info(
        name = "PlayerModel",
        description = "Renders a model of you, or someone you're attacking",
        category = Module.Category.RENDER
)
object PlayerModel : Module() {
    private val scale = register(Settings.integerBuilder("Size").withValue(100).withRange(10, 200).withStep(10))
    private val resetDelay = register(Settings.integerBuilder("ResetDelay").withValue(100).withRange(0, 200).withStep(10))
    private val emulatePitch = register(Settings.b("EmulatePitch", true))
    private val emulateYaw = register(Settings.b("EmulateYaw", false))
    private val x = register(Settings.integerBuilder("X").withValue(200).withRange(0, 2000).withStep(10))
    private val y = register(Settings.integerBuilder("Y").withValue(240).withRange(0, 2000).withStep(10))

    init {
        listener<RenderOverlayEvent> {
            if (mc.player == null || mc.renderManager.renderViewEntity == null) return@listener
            mc.player.lastAttackedEntity.let { attackedEntity: EntityLivingBase? ->
                GlStateUtils.rescaleActual()
                GlStateUtils.depth(true)

                val entity = if (attackedEntity != null && mc.player.ticksExisted - mc.player.lastAttackedEntityTime <= resetDelay.value) attackedEntity else mc.player
                val yaw = if (emulateYaw.value) interpolateAndWrap(entity.prevRotationYaw, entity.rotationYaw) else 0.0f
                val pitch = if (emulatePitch.value) interpolateAndWrap(entity.prevRotationPitch, entity.rotationPitch) else 0.0f
                GuiInventory.drawEntityOnScreen(x.value, y.value, scale.value, -yaw, -pitch, entity)

                GlStateUtils.rescaleMc()
            }
        }
    }

    private fun interpolateAndWrap(prev: Float, current: Float): Float {
        return MathHelper.wrapDegrees(prev + (current - prev) * KamiTessellator.pTicks())
    }
}
