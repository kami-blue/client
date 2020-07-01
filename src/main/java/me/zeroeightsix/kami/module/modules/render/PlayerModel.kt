package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.client.gui.inventory.GuiInventory
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

    private var lastAttackedEntity: EntityLivingBase? = null
    private var pitch = 0f
    private var yaw = 0f

    override fun onUpdate() {
        if (lastAttacked == 0L || lastAttackedEntity == null) {
            lastAttackedEntity = mc.player
            mc.player.setLastAttackedEntity(mc.player)
            lastAttacked = System.currentTimeMillis()
        }

        /* after x seconds of not attacking, reset to mc.player */
        if (lastAttacked + timeout.value * 1000 < System.currentTimeMillis()) {
            lastAttacked = 0
        }

        lastAttackedEntity = mc.player.lastAttackedEntity
        pitch = if (emulatePitch.value) MathHelper.wrapDegrees(mc.player.rotationPitch) else 0.0f
        yaw = if (emulateYaw.value) MathHelper.wrapDegrees(mc.player.rotationYaw) else 0.0f
    }

    override fun onRender() {
        if (lastAttackedEntity == null) return
        GuiInventory.drawEntityOnScreen(x.value, y.value, scale.value, -yaw, -pitch, lastAttackedEntity!!)
    }

    companion object {
        @JvmField
        var lastAttacked: Long = 0
    }
}