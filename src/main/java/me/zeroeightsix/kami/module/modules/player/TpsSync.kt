package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.module.Module

@Module.Info(
        name = "TpsSync",
        description = "Synchronizes block states with the server TPS",
        category = Module.Category.PLAYER
)
object TpsSync : Module()
