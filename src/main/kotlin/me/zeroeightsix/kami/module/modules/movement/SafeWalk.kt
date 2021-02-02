package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.BaritoneUtils

internal object SafeWalk : Module(
    name = "SafeWalk",
    category = Category.MOVEMENT,
    description = "Keeps you from walking off edges"
) {
    private val baritoneCompat = setting("BaritoneCompatibility", true)

    fun shouldSafewalk(): Boolean {
        return isEnabled && (baritoneCompat.value && BaritoneUtils.primary?.customGoalProcess?.goal == null || !baritoneCompat.value)
    }
}