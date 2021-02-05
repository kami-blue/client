package org.kamiblue.client.module.modules.client

import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.color.ColorHolder

internal object GuiColors : Module(
    name = "GuiColors",
    description = "Opens the Click GUI",
    showOnArray = false,
    category = Category.CLIENT,
    alwaysEnabled = true
) {
    private val primarySetting = setting("Primary Color", ColorHolder(111, 166, 222, 255))
    private val outlineSetting = setting("Outline Color", ColorHolder(88, 99, 111, 200))
    private val backgroundSetting = setting("Background Color", ColorHolder(30, 36, 48, 200))
    private val textSetting = setting("Text Color", ColorHolder(255, 255, 255, 255))
    private val aHover = setting("Hover Alpha", 32, 0..255, 1)

    val primary get() = primarySetting.value.clone()
    val idle get() = if (primary.averageBrightness < 0.8f) ColorHolder(255, 255, 255, 0) else ColorHolder(0, 0, 0, 0)
    val hover get() = idle.apply { a = aHover.value }
    val click get() = idle.apply { a = aHover.value * 2 }
    val backGround get() = backgroundSetting.value.clone()
    val outline get() = outlineSetting.value.clone()
    val text get() = textSetting.value.clone()
}