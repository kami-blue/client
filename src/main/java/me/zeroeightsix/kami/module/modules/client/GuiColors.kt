package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.color.ColorHolder

@Module.Info(
    name = "GuiColors",
    description = "Opens the Click GUI",
    showOnArray = false,
    category = Module.Category.CLIENT,
    alwaysEnabled = true
)
object GuiColors : Module() {
    private val primarySetting = setting("PrimaryColor", ColorHolder(155, 144, 255, 240))
    private val outlineSetting = setting("OutlineColor", ColorHolder(111, 111, 122, 200))
    private val backgroundSetting = setting("BackgroundColor", ColorHolder(32, 30, 40, 200))
    private val textSetting = setting("TextColor", ColorHolder(255, 255, 255, 255))
    private val aHover = setting("HoverAlpha", 32, 0..255, 1)

    val primary get() = primarySetting.value.clone()
    val idle get() = if (primary.averageBrightness < 0.8f) ColorHolder(255, 255, 255, 0) else ColorHolder(0, 0, 0, 0)
    val hover get() = idle.apply { a = aHover.value }
    val click get() = idle.apply { a = aHover.value * 2 }
    val backGround get() = backgroundSetting.value.clone()
    val outline get() = outlineSetting.value.clone()
    val text get() = textSetting.value.clone()
}