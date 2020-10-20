package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings

@Module.Info(
        name = "ExtraChatHistory",
        category = Module.Category.CHAT,
        description = "Show more messages in the chat history",
        showOnArray = false
)
object ExtraChatHistory : Module() {
    val maxMessages = register(Settings.integerBuilder("MaxMessage").withValue(1000).withRange(100, 5000).withStep(100))
}