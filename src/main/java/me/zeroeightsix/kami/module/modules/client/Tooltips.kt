package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.module.Module

@Module.Info(
    name = "Tooltips",
    description = "Displays handy module descriptions in the GUI",
    category = Module.Category.CLIENT,
    showOnArray = false,
    enabledByDefault = true
)
object Tooltips : Module()