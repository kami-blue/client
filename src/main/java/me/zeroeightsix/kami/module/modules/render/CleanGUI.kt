package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting

@Module.Info(
        name = "CleanGUI",
        category = Module.Category.RENDER,
        showOnArray = false,
        description = "Modifies parts of the GUI to be transparent"
)
object CleanGUI : Module() {
    val inventoryGlobal = setting("Inventory", true)
    val chatGlobal = setting("Chat", false)
}