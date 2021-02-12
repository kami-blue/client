package org.kamiblue.client.gui.rgui.windows

import org.kamiblue.client.gui.rgui.WindowComponent

/**
 * Window with no rendering
 */
open class CleanWindow(
    name: String,
    posX: Float,
    posY: Float,
    width: Float,
    height: Float,
    settingGroup: SettingGroup
) : WindowComponent(name, posX, posY, width, height, settingGroup)