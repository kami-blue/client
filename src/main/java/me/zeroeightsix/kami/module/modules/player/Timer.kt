package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.mixin.extension.tickLength
import me.zeroeightsix.kami.mixin.extension.timer
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.TickEvent

@Module.Info(
        name = "Timer",
        category = Module.Category.PLAYER,
        description = "Changes your client tick speed"
)
object Timer : Module() {
    private val slow = setting("SlowMode", false)
    private val tickNormal = setting("TickN", 2.0f, 1f..10f, 0.1f, { !slow.value })
    private val tickSlow = setting("TickS", 8f, 1f..10f, 0.1f, { slow.value })

    public override fun onDisable() {
        mc.timer.tickLength = 50.0f
    }

    init {
        safeListener<TickEvent.ClientTickEvent> {
            mc.timer.tickLength =  50.0f /
                    if (!slow.value) tickNormal.value
                    else (tickSlow.value / 10.0f)
        }
    }
}