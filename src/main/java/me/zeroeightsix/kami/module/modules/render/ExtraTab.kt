package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.util.KamiLang 
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting

object ExtraTab : Module(
    name = KamiLang.get("module.modules.render.ExtraTab.Extratab"),
    description = KamiLang.get("module.modules.render.ExtraTab.ExpandsThePlayerTab"),
    category = Category.RENDER
) {
    val tabSize = setting(KamiLang.get("module.modules.render.ExtraTab.Maxplayers"), 265, 80..400, 5)
}