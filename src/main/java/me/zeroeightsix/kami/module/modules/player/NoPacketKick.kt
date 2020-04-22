package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.module.Module

/**
 * @author 086
 * @see me.zeroeightsix.kami.mixin.client.MixinNetworkManager
 *
 * Fixed by 0x2E | PretendingToCode 4/10/2020
 */
@Module.Info(
        name = "NoPacketKick",
        category = Module.Category.PLAYER,
        description = "Suppress network exceptions and prevent getting kicked",
        showOnArray = Module.ShowOnArray.OFF
)
class NoPacketKick : Module()