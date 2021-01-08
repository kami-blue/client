package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module

internal object ChatSetting : Module(
    name = "ChatSetting",
    category = Category.CLIENT,
    description = "Configures chat message manager",
    showOnArray = false,
    alwaysEnabled = true
) {
    val delay = setting("MessageSpeedLimit(s)", 0.5f, 0.1f..20.0f, 0.1f)
    val maxMessageQueueSize = setting("MaxMessageQueueSize", 50, 10..200, 5)
}