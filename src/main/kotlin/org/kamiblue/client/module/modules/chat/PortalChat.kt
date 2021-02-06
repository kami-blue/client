package org.kamiblue.client.module.modules.chat

import org.kamiblue.client.mixin.client.player.MixinEntityPlayerSP
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module

/**
 * @see MixinEntityPlayerSP
 */
internal object PortalChat : Module(
    name = "PortalChat",
    category = Category.CHAT,
    description = "Allows you to open GUIs in portals",
    showOnArray = false
)
