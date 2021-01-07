package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.mixin.client.player.MixinEntityPlayerSP
import me.zeroeightsix.kami.module.Module

/**
 * @see MixinEntityPlayerSP
 */
object PortalChat : Module(
    name = "PortalChat",
    category = Category.CHAT,
    description = "Allows you to open GUIs in portals",
    showOnArray = false
)
