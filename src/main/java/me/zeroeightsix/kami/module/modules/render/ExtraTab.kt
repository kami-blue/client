package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting

@Module.Info(
        name = "ExtraTab",
        description = "Expands the player tab menu",
        category = Module.Category.RENDER
)
object ExtraTab : Module() {
    val tabSize = setting("MaxPlayers", 265, 80..400, 5)
}