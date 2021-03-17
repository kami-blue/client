package org.kamiblue.client.gui.rgui.windows

import org.kamiblue.client.gui.rgui.WindowComponent
import org.kamiblue.client.setting.GuiConfig
import org.kamiblue.client.setting.configs.AbstractConfig
import org.kamiblue.commons.interfaces.Nameable

/**
 * Window with no rendering
 */
open class CleanWindow(
    name: String,
    posX: Float,
    posY: Float,
    width: Float,
    height: Float,
    settingGroup: SettingGroup,
    config: AbstractConfig<out Nameable> = GuiConfig
) : WindowComponent(name, posX, posY, width, height, settingGroup, config)