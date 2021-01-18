package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module

/**
 * Created by 086 on 9/04/2018.
 */
internal object AntiFog : Module(
    name = "AntiFog",
    description = "Disables or reduces fog",
    category = Category.RENDER
) {
    private val mode by setting("Mode", VisionMode.NO_FOG)

    private enum class VisionMode {
        NO_FOG, AIR
    }

    val shouldNoFog get() = isActive() && mode == VisionMode.NO_FOG

    val shouldAir get() = isActive() && mode == VisionMode.AIR

    override fun isActive(): Boolean {
        return isEnabled && mc.player != null && mc.player.ticksExisted > 20
    }
}