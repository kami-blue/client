package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings

@Module.Info(
        name = "CleanGUI",
        category = Module.Category.RENDER,
        showOnArray = Module.ShowOnArray.OFF,
        description = "Modifies parts of the GUI to be transparent"
)
object CleanGUI : Module() {
    val inventoryGlobal = register(Settings.b("Inventory", true))
    val chatGlobal = register(Settings.b("Chat", false))
}