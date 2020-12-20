package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.manager.managers.MessageManager.newMessageModifier
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.text.MessageSendHelper

@Module.Info(
        name = "FormatChat",
        description = "Add color and linebreak support to upstream chat packets",
        category = Module.Category.CHAT,
        modulePriority = 300
)
object FormatChat : Module() {
    private val modifier = newMessageModifier {
        it.packet.message
                .replace('&', '§')
                .replace("#n", "\n")
    }

    override fun onEnable() {
        if (mc.currentServerData == null) {
            MessageSendHelper.sendWarningMessage("$chatName &6&lWarning: &r&6This does not work in singleplayer")
            disable()
        } else {
            MessageSendHelper.sendWarningMessage("$chatName &6&lWarning: &r&6This will kick you on most servers!")
            modifier.enable()
        }
    }

    override fun onDisable() {
        modifier.enable()
    }
}