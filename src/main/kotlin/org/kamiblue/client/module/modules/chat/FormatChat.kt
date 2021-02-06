package org.kamiblue.client.module.modules.chat

import org.kamiblue.client.manager.managers.MessageManager.newMessageModifier
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.text.MessageSendHelper

internal object FormatChat : Module(
    name = "FormatChat",
    description = "Add color and linebreak support to upstream chat packets",
    category = Category.CHAT,
    modulePriority = 300
) {
    private val modifier = newMessageModifier {
        it.packet.message
            .replace('&', '§')
            .replace("#n", "\n")
    }

    init {
        onEnable {
            if (mc.currentServerData == null) {
                MessageSendHelper.sendWarningMessage("$chatName &6&lWarning: &r&6This does not work in singleplayer")
                disable()
            } else {
                MessageSendHelper.sendWarningMessage("$chatName &6&lWarning: &r&6This will kick you on most servers!")
                modifier.enable()
            }
        }

        onDisable {
            modifier.enable()
        }
    }
}