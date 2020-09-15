package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.module.Module

/**
 * @see me.zeroeightsix.kami.mixin.client.MixinNetworkManager
 */
@Module.Info(
        name = "NoPacketKick",
        category = Module.Category.PLAYER,
        description = "Suppress network exceptions and prevent getting kicked",
        showOnArray = Module.ShowOnArray.OFF
)
object NoPacketKick : Module()