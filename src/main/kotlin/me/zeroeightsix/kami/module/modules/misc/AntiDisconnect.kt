package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module

internal object AntiDisconnect : Module(
    name = "AntiDisconnect",
    description = "Are you sure you want to disconnect?",
    category = Category.MISC
) {
    val presses = setting("ButtonPresses", 3, 1..20, 1)
}