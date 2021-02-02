package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module

internal object Tooltips : Module(
    name = "Tooltips",
    description = "Displays handy module descriptions in the GUI",
    category = Category.CLIENT,
    showOnArray = false,
    enabledByDefault = true
)
