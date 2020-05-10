package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings

/**
 * Created (partly) by Dewy on the 19th of April, 2020
 *
 * PLEASE NOTE: Just like xray, the overlay textures will break in a development environment.
 */
@Module.Info(
        name = "HungerOverlay",
        description = "Displays a helpful overlay over your hunger bar.",
        category = Module.Category.PLAYER
)
class HungerOverlay : Module() {
    @JvmField
    var saturationOverlay: Setting<Boolean> = register(Settings.booleanBuilder("Saturation Overlay").withValue(true).build())
    @JvmField
    var foodValueOverlay: Setting<Boolean> = register(Settings.booleanBuilder("Food Value Overlay").withValue(true).build())
    @JvmField
    var foodExhaustionOverlay: Setting<Boolean> = register(Settings.booleanBuilder("Food Exhaustion Overlay").withValue(true).build())
}