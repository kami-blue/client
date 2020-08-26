package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings

/**
 * Created by 086 on 9/04/2018.
 */
@Module.Info(
        name = "AntiFog",
        description = "Disables or reduces fog",
        category = Module.Category.RENDER
)
class AntiFog : Module() {
    enum class VisionMode {
        NO_FOG, AIR
    }

    companion object {
        @JvmField
        var mode: Setting<VisionMode> = Settings.e("Mode", VisionMode.NO_FOG)
        private var INSTANCE = AntiFog()

        @JvmStatic
        fun enabled(): Boolean {
            return INSTANCE.isEnabled
        }
    }

    init {
        INSTANCE = this
        register(mode)
    }
}