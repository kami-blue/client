package me.zeroeightsix.kami.gui.rgui.windows

import me.zeroeightsix.kami.gui.rgui.WindowComponent
import me.zeroeightsix.kami.util.translation.TranslationKey
import me.zeroeightsix.kami.util.translation.TranslationKeyBlank

/**
 * Window with no rendering
 */
open class CleanWindow(
    name: TranslationKey = TranslationKeyBlank(),
    posX: Float,
    posY: Float,
    width: Float,
    height: Float,
    settingGroup: SettingGroup
) : WindowComponent(name, posX, posY, width, height, settingGroup)