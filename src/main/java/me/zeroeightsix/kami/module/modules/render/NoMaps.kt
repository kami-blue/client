package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module

/**
 * Created by littlebroto1 on 8/29/2020
 */

@Module.Info(
        name = "NoMaps",
        category = Module.Category.RENDER,
        description = "Replaces maps with a family friendly alternative"
)
class NoMaps : Module() {
    companion object {
        private var INSTANCE = NoMaps()

        @JvmStatic
        fun enabled(): Boolean {
            return INSTANCE.isEnabled
        }
    }

    init {
        INSTANCE = this
    }
}