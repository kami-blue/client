package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings

/**
 * Code like this does not have an author, as it is literally one function. It's nothing unique.
 * See BowSpam for example. It's just one thing. Anybody can write it the exact same way on accident.
 * There is nothing to credit here.
 * This message is here because clowns decided to argue with me that they should be credited even though they did not come up with the code.
 * Updated by l1ving on 01/03/20
 */
@Module.Info(
        name = "Timer",
        category = Module.Category.PLAYER,
        description = "Changes your client tick speed"
)
object Timer : Module() {
    private val slow = register(Settings.b("SlowMode", false))
    private val tickNormal = register(Settings.floatBuilder("TickN").withValue(2.0f).withRange(1f, 10f).withStep(0.1f).withVisibility { !slow.value }.build())
    private val tickSlow = register(Settings.floatBuilder("TickS").withValue(8f).withRange(1f, 10f).withStep(0.1f).withVisibility { slow.value }.build())

    public override fun onDisable() {
        mc.timer.tickLength = 50.0f
    }

    override fun onUpdate(event: SafeTickEvent) {
        if (!slow.value) {
            mc.timer.tickLength = 50.0f / tickNormal.value
        } else {
            mc.timer.tickLength = 50.0f / (tickSlow.value / 10.0f)
        }
    }
}