package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.manager.managers.MessageManager.newMessageModifier
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module

internal object UwUChat : Module(
    name = "UwUChat",
    description = "Makes your messages lispy",
    category = Category.CHAT,
    modulePriority = 200
) {
    private val modifier = newMessageModifier {
        it.packet.message
            .replace("r", "w")
            .replace("R", "W")
            .replace("l", "w")
            .replace("L", "W")
            .replace(" n", " ny")
            .replace(" N", " Ny")
            .replace("ove", "uv")
            .replace("OVE", "UV")
            .replace("this", "dis")
    }

    init {
        onEnable {
            if (mc.currentServerData == null) {
                modifier.enable()
            } else {
                modifier.enable()
            }
        }

        onDisable {
            modifier.enable()
        }
    }
}