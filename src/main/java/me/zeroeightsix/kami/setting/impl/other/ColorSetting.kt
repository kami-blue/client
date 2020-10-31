package me.zeroeightsix.kami.setting.impl.other

import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.util.color.ColorHolder

class ColorSetting(
        name: String,
        value: ColorHolder,
        val hasAlpha: Boolean = true,
        visibility: () -> Boolean = { true },
        description: String = ""
) : Setting<ColorHolder>(name, value, visibility, { _, input -> input }, description)