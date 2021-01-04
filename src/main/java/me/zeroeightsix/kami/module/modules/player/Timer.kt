package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.mixin.extension.tickLength
import me.zeroeightsix.kami.mixin.extension.timer
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.TickEvent

@Module.Info(
        name = "Timer",
        category = Module.Category.PLAYER,
        description = "Changes your client tick speed"
)
object Timer : Module() {
    private val slow = register(Settings.b("SlowMode", false))
    private val tickNormal = register(Settings.floatBuilder("TickN").withValue(2.0f).withRange(1f, 10f).withStep(0.1f).withVisibility { !slow.value })
    private val tickSlow = register(Settings.floatBuilder("TickS").withValue(8f).withRange(1f, 10f).withStep(0.1f).withVisibility { slow.value })

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