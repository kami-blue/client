package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings

@Module.Info(
        name = "AntiDisconnect",
        description = "Are you sure you want to disconnect?",
        category = Module.Category.MISC
)
object AntiDisconnect : Module() {
    val presses = register(Settings.integerBuilder("ButtonPresses").withValue(3).withRange(1, 20).withStep(1))
}