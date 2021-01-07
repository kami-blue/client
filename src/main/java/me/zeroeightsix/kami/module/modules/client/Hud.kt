package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.color.ColorHolder

object Hud : Module(
    name = "Hud",
    description = "Toggles Hud displaying and settings",
    category = Category.CLIENT,
    showOnArray = false,
    enabledByDefault = true
) {
    val hudFrame by setting("HudFrame", false)
    val primaryColor by setting("PrimaryColor", ColorHolder(255, 255, 255), false)
    val secondaryColor by setting("SecondaryColor", ColorHolder(155, 144, 255), false)
}