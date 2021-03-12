package org.kamiblue.client.module.modules.player

import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.manager.managers.TimerManager.modifyTimer
import org.kamiblue.client.manager.managers.TimerManager.resetTimer
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.atFalse
import org.kamiblue.client.util.atTrue
import org.kamiblue.event.listener.listener

internal object Timer : Module(
    name = "Timer",
    category = Category.PLAYER,
    description = "Changes your client tick speed",
    modulePriority = 500
) {
    private val slow0 = setting("Slow Mode", false)
    private val slow by slow0
    private val tickNormal by setting("Tick N", 2.0f, 1f..10f, 0.1f, slow0.atFalse())
    private val tickSlow by setting("Tick S", 8f, 1f..10f, 0.1f, slow0.atTrue())

    init {
        onDisable {
            resetTimer()
        }

        listener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@listener

            val multiplier = if (!slow) tickNormal else tickSlow / 10.0f
            modifyTimer(50.0f / multiplier)
        }
    }
}