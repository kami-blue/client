package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module

internal object ExtraTab : Module(
    name = "ExtraTab",
    description = "Expands the player tab menu",
    category = Category.RENDER
) {
    val tabSize = setting("MaxPlayers", 265, 80..400, 5)
}