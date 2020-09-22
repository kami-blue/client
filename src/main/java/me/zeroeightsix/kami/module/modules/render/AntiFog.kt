package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings

/**
 * Created by 086 on 9/04/2018.
 */
@Module.Info(
        name = "AntiFog",
        description = "Disables or reduces fog",
        category = Module.Category.RENDER
)
object AntiFog : Module() {
    val mode = register(Settings.e<VisionMode>("Mode", VisionMode.NO_FOG))

    enum class VisionMode {
        NO_FOG, AIR
    }
}