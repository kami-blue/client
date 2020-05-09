package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.module.Module

/**
 * Created by 086 on 12/12/2017.
 *
 * @see me.zeroeightsix.kami.mixin.client.MixinEntityPlayerSP
 */
@Module.Info(
        name = "PortalChat",
        category = Module.Category.CHAT,
        description = "Allows you to open GUIs in portals",
        showOnArray = Module.ShowOnArray.OFF
)
class PortalChat : Module()
