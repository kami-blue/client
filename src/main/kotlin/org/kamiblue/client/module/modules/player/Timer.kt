package org.kamiblue.client.module.modules.player

import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.mixin.extension.tickLength
import org.kamiblue.client.mixin.extension.timer
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.threads.safeListener

internal object Timer : Module(
    name = "Timer",
    category = Category.PLAYER,
    description = "Changes your client tick speed"
) {
    private val slow by setting("Slow Mode", false)
    private val tickNormal by setting("Tick N", 2.0f, 1f..10f, 0.1f, { !slow })
    private val tickSlow by setting("Tick S", 8f, 1f..10f, 0.1f, { slow })

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