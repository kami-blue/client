package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting

object AntiDisconnect : Module(
    category = Category.MISC
) {
    val presses = setting("ButtonPresses", 3, 1..20, 1)
}