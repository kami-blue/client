package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.graphics.font.KamiFontRenderer

@Module.Info(
    name = "CustomFont",
    description = "Use the better font instead of the stupid Minecraft font",
    showOnArray = false,
    category = Module.Category.CLIENT,
    enabledByDefault = true
)
object CustomFont : Module() {
    private const val DEFAULT_FONT_NAME = "Source Sans Pro"

    val fontName = setting("FontName",
        DEFAULT_FONT_NAME,
        consumer = { prev: String, value: String -> getMatchingFontName(value) ?: prev })

    private val sizeSetting = setting("Size", 1.0f, 0.5f..2.0f, 0.05f)
    private val gapSetting = setting("Gap", 0.0f, -10f..10f, 0.5f)
    private val lineSpaceSetting = setting("LineSpace", 0.0f, -10f..10f, 0.5f)
    private val baselineOffsetSetting = setting("BaselineOffset", 0.0f, -10.0f..10.0f, 0.25f)
    private val lodBiasSetting = setting("LodBias", 2.0f, 0.0f..5.0f, 0.05f)

    val isDefaultFont get() = fontName.value.equals(DEFAULT_FONT_NAME, true)
    val size get() = sizeSetting.value * 0.15f
    val gap get() = gapSetting.value * 0.5f - 0.8f
    val lineSpace get() = size * (lineSpaceSetting.value * 0.05f + 0.8f)
    val lodBias get() = lodBiasSetting.value * 0.5f - 1.25f
    val baselineOffset get() = baselineOffsetSetting.value - 4.0f

    private fun getMatchingFontName(name: String): String? {
        return if (name.equals(DEFAULT_FONT_NAME, true)) DEFAULT_FONT_NAME
        else KamiFontRenderer.availableFonts.firstOrNull { it.equals(name, true) }
    }

    init {
        fontName.listeners.add {
            if (mc.isCallingFromMinecraftThread) KamiFontRenderer.reloadFonts()
        }
    }
}