package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module

internal object ExtraChatHistory : Module(
    name = "ExtraChatHistory",
    category = Category.CHAT,
    description = "Show more messages in the chat history",
    showOnArray = false
) {
    val maxMessages = setting("MaxMessage", 1000, 100..5000, 100)
}