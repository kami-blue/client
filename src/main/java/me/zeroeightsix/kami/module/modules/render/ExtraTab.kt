package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings

/**
 * Created by 086 on 8/04/2018.
 * Optimized by dominikaaaa on 28/05/20
 */
@Module.Info(
        name = "ExtraTab",
        description = "Expands the player tab menu",
        category = Module.Category.RENDER
)
class ExtraTab : Module() {
    @JvmField
    var offload: Setting<Boolean> = register(Settings.b("ReduceLag", true))

    @JvmField
    var tabSize: Setting<Int> = register(Settings.integerBuilder("MaxPlayers").withMinimum(1).withValue(230).build())

    companion object {
        @JvmField
        var INSTANCE: ExtraTab? = null
    }

    init {
        INSTANCE = this
    }
}