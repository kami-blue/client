package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings

/**
 * @author Humboldt123
 * Reverse engineered from Impact Client
 */
@Module.Info(
        name = "Franky",
        category = Module.Category.MISC,
        description = "Does exactly what you think it does"
)
class Franky : Module() {
    private val maxExploit = register(Settings.b("MaxExploit", false))
    private val bigrat = register(Settings.b("Bigrat", false))
} 
