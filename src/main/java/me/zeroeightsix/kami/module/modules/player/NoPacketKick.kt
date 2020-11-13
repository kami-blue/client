package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.mixin.client.network.MixinNetworkManager
import me.zeroeightsix.kami.module.Module

/**
 * @see MixinNetworkManager
 */
@Module.Info(
        name = "NoPacketKick",
        category = Module.Category.PLAYER,
        description = "Suppress network exceptions and prevent getting kicked",
        showOnArray = false
)
object NoPacketKick : Module()