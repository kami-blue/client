package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting

@Module.Info(
    name = "ChatSetting",
    category = Module.Category.CLIENT,
    description = "Configures chat message manager",
    showOnArray = false,
    alwaysEnabled = true
)
object ChatSetting : Module() {
    val delay = setting("MessageSpeedLimit(s)", 0.5f, 0.1f..20.0f, 0.1f)
    val maxMessageQueueSize = setting("MaxMessageQueueSize", 50, 10..200, 5)
}