package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting

@Module.Info(
        name = "ChatSetting",
        category = Module.Category.CLIENT,
        description = "Configures chat message manager",
        showOnArray = Module.ShowOnArray.OFF,
        alwaysEnabled = true
)
object ChatSetting : Module() {
    val delay = setting("MessageSpeedLimit(s)", 3.0f, 1.0f..20.0f, 0.5f)
    val maxMessageQueueSize = setting("MaxMessageQueueSize", 50, 10..200, 5)
}