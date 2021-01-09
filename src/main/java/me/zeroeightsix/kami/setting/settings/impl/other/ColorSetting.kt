package me.zeroeightsix.kami.setting.settings.impl.other

import me.zeroeightsix.kami.setting.settings.MutableSetting
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.translation.TranslationKey
import me.zeroeightsix.kami.util.translation.TranslationKeyBlank

class ColorSetting(
        name: TranslationKey,
        value: ColorHolder,
        val hasAlpha: Boolean = true,
        visibility: () -> Boolean = { true },
        description: TranslationKey = TranslationKeyBlank()
) : MutableSetting<ColorHolder>(name, value, visibility, { _, input -> if (!hasAlpha) input.apply { a = 255 } else input }, description)