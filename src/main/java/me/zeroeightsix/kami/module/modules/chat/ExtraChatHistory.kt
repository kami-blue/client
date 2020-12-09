package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting

@Module.Info(
    name = "ExtraChatHistory",
    category = Module.Category.CHAT,
    description = "Show more messages in the chat history",
    showOnArray = false
)
object ExtraChatHistory : Module() {
    val maxMessages = setting("MaxMessage", 1000, 100..5000, 100)
}