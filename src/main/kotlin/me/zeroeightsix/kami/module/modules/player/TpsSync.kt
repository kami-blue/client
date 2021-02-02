package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module

internal object TpsSync : Module(
    name = "TpsSync",
    description = "Synchronizes block states with the server TPS",
    category = Category.PLAYER
)
