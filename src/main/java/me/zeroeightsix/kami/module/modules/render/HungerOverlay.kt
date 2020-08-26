package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings

/**
 * Created (partly) by Dewy on the 19th of April, 2020
 *
 * PLEASE NOTE: Just like xray, the overlay textures will break in a development environment.
 *
 * TODO: @see [me.zeroeightsix.kami.event.ForgeEventProcessor]
 */
@Module.Info(
        name = "HungerOverlay",
        description = "Displays a helpful overlay over your hunger bar.",
        category = Module.Category.PLAYER
)
class HungerOverlay : Module() {
    @JvmField val saturationOverlay: Setting<Boolean> = register(Settings.booleanBuilder("SaturationOverlay").withValue(true).build())
    @JvmField val foodValueOverlay: Setting<Boolean> = register(Settings.booleanBuilder("FoodValueOverlay").withValue(true).build())
    @JvmField val foodExhaustionOverlay: Setting<Boolean> = register(Settings.booleanBuilder("FoodExhaustionOverlay").withValue(true).build())
}