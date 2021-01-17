package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting

object CleanGUI : Module(
    category = Category.RENDER,
    showOnArray = false,
) {
    val inventoryGlobal = setting("Inventory", true)
    val chatGlobal = setting("Chat", false)
}