package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings

@Module.Info(
        name = "ChatSetting",
        category = Module.Category.CLIENT,
        description = "Configures chat message manager",
        showOnArray = Module.ShowOnArray.OFF,
        alwaysEnabled = true
)
object ChatSetting : Module() {
    val delay = register(Settings.floatBuilder("MessageSpeedLimit(s)").withValue(0.5f).withRange(0.1f, 20.0f).withStep(0.1f))
    val maxMessageQueueSize = register(Settings.integerBuilder("MaxMessageQueueSize").withValue(50).withRange(10, 200).withStep(5))
}
