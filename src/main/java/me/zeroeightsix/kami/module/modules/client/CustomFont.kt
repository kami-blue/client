package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.graphics.font.KamiFontRenderer
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.formatValue

@Module.Info(
    name = "CustomFont",
    description = "Use the better font instead of the stupid Minecraft font",
    showOnArray = Module.ShowOnArray.OFF,
    category = Module.Category.CLIENT,
    enabledByDefault = true
)
object CustomFont : Module() {
    private const val DEFAULT_FONT_NAME = "Source Sans Pro"

    val fontName = register(Settings.stringBuilder("FontName")
        .withValue(DEFAULT_FONT_NAME)
        .withRestriction { getMatchingFontName(it) != null }
        .withConsumer { _: String, value: String -> getMatchingFontName(value) })
    private val sizeSetting = register(Settings.floatBuilder("Size").withValue(1.0f).withRange(0.5f, 2.0f).withStep(0.05f))
    private val gapSetting = register(Settings.floatBuilder("Gap").withValue(0.0f).withRange(-10f, 10f).withStep(0.5f))
    private val lineSpaceSetting = register(Settings.floatBuilder("LineSpace").withValue(0.0f).withRange(-10f, 10f).withStep(0.5f))
    private val baselineOffsetSetting = register(Settings.floatBuilder("BaselineOffset").withValue(0.0f).withRange(-10.0f, 10.0f).withStep(0.25f))
    private val lodBiasSetting = register(Settings.floatBuilder("LodBias").withValue(2.0f).withRange(0.0f, 5.0f).withStep(0.05f))

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

    override fun onToggle() {
        MessageSendHelper.sendChatMessage(
            "Changed font! Run \n" +
                "&7${CommandManager.prefix}config save\n" +
                "&7${CommandManager.prefix}config reload\n" +
                "&f if it's not sizing correctly"
        )
        if (isDefaultFont) {
            MessageSendHelper.sendChatMessage("You can run " +
                "${formatValue("${CommandManager.prefix}set $originalName ${fontName.name} <Font Name>")} " +
                "to change custom font"
            )
        }
    }

    init {
        fontName.settingListener = Setting.SettingListeners {
            if (mc.isCallingFromMinecraftThread) KamiFontRenderer.reloadFonts()
        }
    }
}