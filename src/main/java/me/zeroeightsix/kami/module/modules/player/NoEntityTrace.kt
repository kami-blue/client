package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings

@Module.Info(
        name = "NoEntityTrace",
        category = Module.Category.PLAYER,
        description = "Blocks entities from stopping you from mining"
)
object NoEntityTrace : Module() {
    private val mode = register(Settings.e<TraceMode>("Mode", TraceMode.DYNAMIC))

    private enum class TraceMode {
        STATIC, DYNAMIC
    }

    @JvmStatic
    fun shouldBlock(): Boolean {
        return isEnabled && (mode.value == TraceMode.STATIC || mc.playerController.isHittingBlock)
    }
}