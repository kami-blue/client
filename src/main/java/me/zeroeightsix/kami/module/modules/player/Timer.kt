package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.mixin.extension.tickLength
import me.zeroeightsix.kami.mixin.extension.timer
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.TickEvent

internal object Timer : Module(
    name = "Timer",
    category = Category.PLAYER,
    description = "Changes your client tick speed"
) {
    private val slow by setting("SlowMode", false)
    private val tickNormal by setting("TickN", 2.0f, 1f..10f, 0.1f, { !slow })
    private val tickSlow by setting("TickS", 8f, 1f..10f, 0.1f, { slow })

    init {
        onDisable {
            mc.timer.tickLength = 50.0f
        }

        safeListener<TickEvent.ClientTickEvent> {
            mc.timer.tickLength = 50.0f /
                if (!slow) tickNormal
                else (tickSlow / 10.0f)
        }
    }
}