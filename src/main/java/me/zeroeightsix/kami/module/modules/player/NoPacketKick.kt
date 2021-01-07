package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.mixin.client.network.MixinNetworkManager
import me.zeroeightsix.kami.module.Module

/**
 * @see MixinNetworkManager
 */
object NoPacketKick : Module(
    name = "NoPacketKick",
    category = Category.PLAYER,
    description = "Suppress network exceptions and prevent getting kicked",
    showOnArray = false
)
