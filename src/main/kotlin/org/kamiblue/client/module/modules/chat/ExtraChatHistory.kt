package org.kamiblue.client.module.modules.chat

import net.minecraft.client.gui.ChatLine
import net.minecraft.util.text.ITextComponent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

internal object ExtraChatHistory : Module(
    name = "ExtraChatHistory",
    alias = arrayOf("InfiniteChat", "InfiniteChatHistory"),
    category = Category.CHAT,
    description = "Show more messages in the chat history",
    showOnArray = false
) {
    private val maxMessages by setting("Max Message", 1000, 100..5000, 100)

    @JvmStatic
    fun handleSetChatLine(
        drawnChatLines: MutableList<ChatLine>,
        chatLines: MutableList<ChatLine>,
        chatComponent: ITextComponent,
        chatLineId: Int,
        updateCounter: Int,
        displayOnly: Boolean,
        ci: CallbackInfo
    ) {
        if (isDisabled) return

        while (drawnChatLines.isNotEmpty() && drawnChatLines.size > maxMessages) {
            drawnChatLines.removeLast()
        }

        if (!displayOnly) {
            chatLines.add(0, ChatLine(updateCounter, chatComponent, chatLineId))

            while (chatLines.isNotEmpty() && chatLines.size > maxMessages) {
                chatLines.removeLast()
            }
        }

        ci.cancel()
    }
}