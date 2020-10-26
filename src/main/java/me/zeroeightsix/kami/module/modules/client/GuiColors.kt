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
    private val rPrimary = setting("PrimaryRed", 155, 0..255, 1)
    private val gPrimary = setting("PrimaryGreen", 144, 0..255, 1)
    private val bPrimary = setting("PrimaryBlue", 255, 0..255, 1)
    private val aPrimary = setting("PrimaryAlpha", 240, 0..255, 1)

    private val rOutline = setting("OutlineRed", 175, 0..255, 1)
    private val gOutline = setting("OutlineGreen", 171, 0..255, 1)
    private val bOutline = setting("OutlineBlue", 204, 0..255, 1)
    private val aOutline = setting("OutlineAlpha", 127, 0..255, 1)

    private val rBg = setting("BackgroundRed", 32, 0..255, 1)
    private val gBg = setting("BackgroundGreen", 30, 0..255, 1)
    private val bBg = setting("BackgroundBlue", 40, 0..255, 1)
    private val aBg = setting("BackgroundAlpha", 200, 0..255, 1)

    private val rText = setting("TextRed", 255, 0..255, 1)
    private val gText = setting("TextGreen", 255, 0..255, 1)
    private val bText = setting("TextBlue", 255, 0..255, 1)
    private val aText = setting("TextAlpha", 255, 0..255, 1)

    private val aHover = setting("HoverAlpha", 16, 0..255, 1)

    val primary get() = ColorHolder(rPrimary.value, gPrimary.value, bPrimary.value, aPrimary.value)
    val idle get() = if (primary.averageBrightness < 0.9f) ColorHolder(255, 255, 255, 0) else ColorHolder(0, 0, 0, 0)
    val hover get() = idle.apply { a = aHover.value }
    val click get() = idle.apply { a = aHover.value * 2 }
    val backGround get() = ColorHolder(rBg.value, gBg.value, bBg.value, aBg.value)
    val outline get() = ColorHolder(rOutline.value, gOutline.value, bOutline.value, aOutline.value)
    val text get() = ColorHolder(rText.value, gText.value, bText.value, aText.value)

    private fun ColorHolder.variant(diff: Float) =
            if (brightness > 0.5f) multiply(1f - diff)
            else multiply(1f + diff)
}