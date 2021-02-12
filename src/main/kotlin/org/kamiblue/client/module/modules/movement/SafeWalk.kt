package org.kamiblue.client.module.modules.movement

import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.BaritoneUtils

internal object SafeWalk : Module(
    name = "SafeWalk",
    category = Category.MOVEMENT,
    description = "Keeps you from walking off edges"
) {
    private val baritoneCompat = setting("Baritone Compatibility", true)

    fun shouldSafewalk(): Boolean {
        return isEnabled && (baritoneCompat.value && BaritoneUtils.primary?.customGoalProcess?.goal == null || !baritoneCompat.value)
    }
}