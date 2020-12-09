package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting

@Module.Info(
    name = "AntiDisconnect",
    description = "Are you sure you want to disconnect?",
    category = Module.Category.MISC
)
object AntiDisconnect : Module() {
    val presses = setting("ButtonPresses", 3, 1..20, 1)
}