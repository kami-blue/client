package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.module.Module

/**
 * Created by 086 on 9/04/2018.
 */
@Module.Info(
        name = "TpsSync",
        description = "Synchronizes block states with the server TPS",
        category = Module.Category.PLAYER
)
class TpsSync : Module()
