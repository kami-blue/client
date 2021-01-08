package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.util.KamiLang 
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting

object CleanGUI : Module(
    name = KamiLang.get("module.modules.render.CleanGUI.Cleangui"),
    category = Category.RENDER,
    showOnArray = false,
    description = KamiLang.get("module.modules.render.CleanGUI.ModifiesPartsOfThe")
) {
    val inventoryGlobal = setting(KamiLang.get("module.modules.render.CleanGUI.Inventory"), true)
    val chatGlobal = setting(KamiLang.get("module.modules.render.CleanGUI.Chat"), false)
}