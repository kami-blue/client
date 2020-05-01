package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings

@Module.Info(
        name = "AntiDisconnect",
        description = "Are you sure you want to disconnect?",
        category = Module.Category.MISC
)
class AntiDisconnect : Module() {
    @JvmField
    var requiredButtonPresses: Setting<Int> = register(Settings.integerBuilder("Button Presses").withMinimum(1).withMaximum(20).withValue(3).build())
}