package me.zeroeightsix.kami.gui.mc

import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.mixin.extension.historyBuffer
import me.zeroeightsix.kami.mixin.extension.sentHistoryCursor
import net.minecraft.client.gui.GuiChat
import java.util.*

open class KamiGuiChat(startStringIn: String, historyBufferIn: String, sentHistoryCursorIn: Int) : GuiChat(startStringIn) {

    init {
        historyBuffer = historyBufferIn
        sentHistoryCursor = sentHistoryCursorIn
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        super.keyTyped(typedChar, keyCode)

        if (!inputField.text.startsWith(CommandManager.prefix.value)) {
            displayNormalChatGUI()
        }
    }

    private fun displayNormalChatGUI() {
        GuiChat(inputField.text).apply {
            historyBuffer = this@KamiGuiChat.historyBuffer
            sentHistoryCursor = this@KamiGuiChat.sentHistoryCursor
        }.also {
            mc.displayGuiScreen(it)
        }
    }

}