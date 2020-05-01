package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.module.Module

/**
 * Created by 086 on 8/04/2018.
 */
@Module.Info(
        name = "AntiWeather",
        description = "Removes rain from your world",
        category = Module.Category.MISC
)
class AntiWeather : Module() {
    override fun onUpdate() {
        if (isDisabled) return
        if (mc.world.isRaining) mc.world.setRainStrength(0f)
    }
}