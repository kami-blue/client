package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.module.Module

/**
 * Created by Xiaro on 19/07/20
 */
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
class AutoOffhand : Module() {
    private val idleItem = null
    private enum class IdleItem {
        TOTEM, SHIELD
    }
}