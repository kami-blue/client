package org.kamiblue.client.module.modules.client

import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module

internal object ChatSetting : Module(
    name = "ChatSetting",
    category = Category.CLIENT,
    description = "Configures chat message manager",
    showOnArray = false,
    alwaysEnabled = true
) {
    val delay = setting("Message Speed Limit", 0.5f, 0.1f..20.0f, 0.1f, description = "Delay between each message in seconds")
    val maxMessageQueueSize = setting("Max Message Queue Size", 50, 10..200, 5)
}