package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.module.Module

// TODO: Merge Auto totem into this
// TODO: Merge OffhandGap into this
// TODO: #1329
// TODO: #1045
// TODO: #1049
// TODO: #1005
// TODO: #943
// TODO: #836
@Module.Info(
        name = "AutoOffhand",
        description = "Manage item in your offhand",
        category = Module.Category.COMBAT
)
object AutoOffhand : Module() {
    private val idleItem = null
    private enum class IdleItem {
        TOTEM, SHIELD
    }
}