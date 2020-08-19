package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.KamiTessellator
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.math.MathHelper

/**
 * @author dominikaaaa
 * Ngl this code is so fucking scuffed :joy_cat:
 * It should be illegal to write code at 2am
 *
 * tldr the way this works is by modifying mc.player.lastAttackedEntity you can make this compatible with lots of stuff
 * *but* it defaults to mc.player if 1. the time is reset or 2. your target disconnects / dies
 */
@Module.Info(
        name = "PlayerModel",
        description = "Renders a model of you, or someone you're attacking",
        category = Module.Category.RENDER
)
class PlayerModel : Module() {
    private val scale = register(Settings.integerBuilder("Size").withRange(1, 100).withValue(50).build())
    private val timeout = register(Settings.integerBuilder("ResetTimeout").withRange(1, 100).withValue(10).build())
    private val emulatePitch = register(Settings.b("EmulatePitch", true))
    private val emulateYaw = register(Settings.b("EmulateYaw", false))
    private val x = register(Settings.i("X", 100))
    private val y = register(Settings.i("Y", 120))

    private var entity: EntityLivingBase? = null

    override fun onUpdate() {
        if (lastAttacked == 0L || entity == null) {
            entity = mc.player
            mc.player.setLastAttackedEntity(mc.player)
            lastAttacked = System.currentTimeMillis()
        }

        /* after x seconds of not attacking, reset to mc.player */
        if (lastAttacked + timeout.value * 1000 < System.currentTimeMillis()) {
            lastAttacked = 0
        }

        entity = mc.player.lastAttackedEntity
    }

    override fun onRender() {
        if (entity == null) return
        GlStateManager.pushMatrix()
        GlStateManager.enableDepth()
        val yaw = if (emulateYaw.value) interpolateAndWrap(entity!!.prevRotationYaw, entity!!.rotationYaw) else 0.0f
        val pitch = if (emulatePitch.value) interpolateAndWrap(entity!!.prevRotationPitch, entity!!.rotationPitch) else 0.0f
        GuiInventory.drawEntityOnScreen(x.value, y.value, scale.value, -yaw, -pitch, entity!!)
        GlStateManager.disableDepth()
        GlStateManager.popMatrix()
    }

    private fun interpolateAndWrap(prev: Float, current: Float): Float {
        return MathHelper.wrapDegrees(prev + (current - prev) * KamiTessellator.pTicks())
    }

    companion object {
        @JvmField
        var lastAttacked: Long = 0
    }
}