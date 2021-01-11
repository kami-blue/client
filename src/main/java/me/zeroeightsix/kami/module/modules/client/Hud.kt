package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.color.ColorHolder

object Hud : Module(
    category = Category.CLIENT,
    showOnArray = false,
    enabledByDefault = true
) {
    val hudFrame by setting(getTranslationKey("HudFrame"), false)
    val primaryColor by setting(getTranslationKey("PrimaryColor"), ColorHolder(255, 255, 255), false)
    val secondaryColor by setting(getTranslationKey("SecondaryColor"), ColorHolder(155, 144, 255), false)
}