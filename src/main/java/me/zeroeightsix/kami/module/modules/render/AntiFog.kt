package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.util.KamiLang 
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting

/**
 * Created by 086 on 9/04/2018.
 */
object AntiFog : Module(
    name = KamiLang.get("module.modules.render.AntiFog.Antifog"),
    description = KamiLang.get("module.modules.render.AntiFog.DisablesOrReducesFog"),
    category = Category.RENDER
) {
    val mode = setting(KamiLang.get("module.modules.render.AntiFog.Mode"), VisionMode.NO_FOG)

    enum class VisionMode {
        NO_FOG, AIR
    }
}