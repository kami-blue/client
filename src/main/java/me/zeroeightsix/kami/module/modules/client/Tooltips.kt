package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.module.Module

/**
 * @author l1ving
 * @see me.zeroeightsix.kami.gui.kami.theme.kami.RootCheckButtonUI
 */
@Module.Info(
        name = "Tooltips",
        description = "Displays handy module descriptions in the GUI",
        category = Module.Category.CLIENT,
        showOnArray = Module.ShowOnArray.OFF,
        enabledByDefault = true
)
object Tooltips : Module()