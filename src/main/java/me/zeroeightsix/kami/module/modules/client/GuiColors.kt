package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.color.ColorHolder

@Module.Info(
        name = "GuiColors",
        description = "Opens the Click GUI",
        showOnArray = Module.ShowOnArray.OFF,
        category = Module.Category.CLIENT,
        alwaysEnabled = true
)
object GuiColors : Module() {
    private val rPrimary = register(Settings.integerBuilder("PrimaryRed").withValue(155).withRange(0, 255).withStep(1))
    private val gPrimary = register(Settings.integerBuilder("PrimaryGreen").withValue(144).withRange(0, 255).withStep(1))
    private val bPrimary = register(Settings.integerBuilder("PrimaryBlue").withValue(255).withRange(0, 255).withStep(1))
    private val aPrimary = register(Settings.integerBuilder("PrimaryAlpha").withValue(240).withRange(0, 255).withStep(1))

    private val rOutline = register(Settings.integerBuilder("OutlineRed").withValue(175).withRange(0, 255).withStep(1))
    private val gOutline = register(Settings.integerBuilder("OutlineGreen").withValue(171).withRange(0, 255).withStep(1))
    private val bOutline = register(Settings.integerBuilder("OutlineBlue").withValue(204).withRange(0, 255).withStep(1))
    private val aOutline = register(Settings.integerBuilder("OutlineAlpha").withValue(127).withRange(0, 255).withStep(1))

    private val rBg = register(Settings.integerBuilder("BackgroundRed").withValue(32).withRange(0, 255).withStep(1))
    private val gBg = register(Settings.integerBuilder("BackgroundGreen").withValue(30).withRange(0, 255).withStep(1))
    private val bBg = register(Settings.integerBuilder("BackgroundBlue").withValue(40).withRange(0, 255).withStep(1))
    private val aBg = register(Settings.integerBuilder("BackgroundAlpha").withValue(200).withRange(0, 255).withStep(1))

    private val rText = register(Settings.integerBuilder("TextRed").withValue(255).withRange(0, 255).withStep(1))
    private val gText = register(Settings.integerBuilder("TextGreen").withValue(255).withRange(0, 255).withStep(1))
    private val bText = register(Settings.integerBuilder("TextBlue").withValue(255).withRange(0, 255).withStep(1))
    private val aText = register(Settings.integerBuilder("TextAlpha").withValue(255).withRange(0, 255).withStep(1))

    val primary get() = ColorHolder(rPrimary.value, gPrimary.value, bPrimary.value, aPrimary.value)
    val hover get() = primary.variant(0.1f)
    val click get() = primary.variant(0.2f)
    val backGround get() = ColorHolder(rBg.value, gBg.value, bBg.value, aBg.value)
    val outline get() = ColorHolder(rOutline.value, gOutline.value, bOutline.value, aOutline.value)
    val text get() = ColorHolder(rText.value, gText.value, bText.value, aText.value)

    private fun ColorHolder.variant(diff: Float) =
            if (brightness > 0.5f) multiply(1f - diff)
            else multiply(1f + diff)
}