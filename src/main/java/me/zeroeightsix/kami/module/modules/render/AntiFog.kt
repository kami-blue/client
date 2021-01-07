package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting

/**
 * Created by 086 on 9/04/2018.
 */
object AntiFog : Module(
    name = "AntiFog",
    description = "Disables or reduces fog",
    category = Category.RENDER
) {
    val mode = setting("Mode", VisionMode.NO_FOG)

    enum class VisionMode {
        NO_FOG, AIR
    }
}