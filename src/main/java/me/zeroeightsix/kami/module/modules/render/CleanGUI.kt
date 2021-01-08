package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module

internal object CleanGUI : Module(
    name = "CleanGUI",
    category = Category.RENDER,
    showOnArray = false,
    description = "Modifies parts of the GUI to be transparent"
) {
    val inventoryGlobal = setting("Inventory", true)
    val chatGlobal = setting("Chat", false)
}