package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.color.ColorHolder

@Module.Info(
        name = "ClickGUI",
        description = "Opens the Click GUI",
        showOnArray = Module.ShowOnArray.OFF,
        category = Module.Category.CLIENT,
        alwaysEnabled = true
)
object GuiColors : Module() {
    private val rPrimary = register(Settings.integerBuilder("Primary Red").withValue(116).withRange(0, 255).withStep(1))
    private val gPrimary = register(Settings.integerBuilder("Primary Green").withValue(107).withRange(0, 255).withStep(1))
    private val bPrimary = register(Settings.integerBuilder("Primary Blue").withValue(191).withRange(0, 255).withStep(1))
    private val aPrimary = register(Settings.integerBuilder("Primary Alpha").withValue(240).withRange(0, 255).withStep(1))

    private val rBg = register(Settings.integerBuilder("BackgroundRed").withValue(39).withRange(0, 255).withStep(1))
    private val gBg = register(Settings.integerBuilder("BackgroundGreen").withValue(36).withRange(0, 255).withStep(1))
    private val bBg = register(Settings.integerBuilder("BackgroundBlue").withValue(64).withRange(0, 255).withStep(1))
    private val aBg = register(Settings.integerBuilder("BackgroundAlpha").withValue(200).withRange(0, 255).withStep(1))

    private val rText = register(Settings.integerBuilder("TextRed").withValue(255).withRange(0, 255).withStep(1))
    private val gText = register(Settings.integerBuilder("TextGreen").withValue(255).withRange(0, 255).withStep(1))
    private val bText = register(Settings.integerBuilder("TextBlue").withValue(255).withRange(0, 255).withStep(1))
    private val aText = register(Settings.integerBuilder("TextAlpha").withValue(255).withRange(0, 255).withStep(1))

    val primary get() = ColorHolder(rPrimary.value, gPrimary.value, bPrimary.value, aPrimary.value)
    val primaryHover get() = primary.variant(0.2f)
    val primaryClicked get() = primary.variant(0.4f)
    val backGround get() = ColorHolder(rBg.value, gBg.value, bBg.value, aBg.value)
    val text get() = ColorHolder(rText.value, gText.value, bText.value, aText.value)

    private fun ColorHolder.variant(diff: Float) =
            if (brightness > 0.5f) normalized().multiply(brightness - diff)
            else normalized().multiply(brightness + diff)
}