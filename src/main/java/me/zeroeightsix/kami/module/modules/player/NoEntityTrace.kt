package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings

/**
 * Created by 086 on 8/04/2018.
 */
@Module.Info(
        name = "NoEntityTrace",
        category = Module.Category.PLAYER,
        description = "Blocks entities from stopping you from mining"
)
class NoEntityTrace : Module() {
    private val mode = register(Settings.e<TraceMode>("Mode", TraceMode.DYNAMIC))

    private enum class TraceMode {
        STATIC, DYNAMIC
    }

    companion object {
        private var INSTANCE: NoEntityTrace? = null
        @JvmStatic
        fun shouldBlock(): Boolean {
            return INSTANCE!!.isEnabled && (INSTANCE!!.mode.value == TraceMode.STATIC || mc.playerController.isHittingBlock)
        }
    }

    init {
        INSTANCE = this
    }
}