package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module

@Module.Info(
        name = "AntiWeather",
        description = "Removes rain from your world",
        category = Module.Category.MISC
)
object AntiWeather : Module() {
    override fun onUpdate(event: SafeTickEvent) {
        if (mc.world.isRaining) mc.world.setRainStrength(0f)
        if (mc.world.isThundering) mc.world.setThunderStrength(0f)
    }
}