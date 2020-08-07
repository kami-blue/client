package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.module.Module

/**
 * Created by Xiaro on 19/07/20
 */
@Module.Info(
        name = "AutoOffhand",
        description = "Manage item in your offhand",
        category = Module.Category.COMBAT
)
class AutoOffhand : Module() {
    private val idleItem = null
    private enum class IdleItem {
        TOTEM, SHIELD
    }
}