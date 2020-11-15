@file:Suppress("DEPRECATION")

package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.color.ColorConverter.rgbToHex
import me.zeroeightsix.kami.util.color.ColorTextFormatting
import me.zeroeightsix.kami.util.math.MathUtils.isNumberEven
import me.zeroeightsix.kami.util.math.MathUtils.reverseNumber
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import net.minecraft.util.text.TextFormatting
import java.awt.Color

@Module.Info(
        name = "ActiveModules",
        category = Module.Category.CLIENT,
        description = "Configures ActiveModules colours and modes",
        showOnArray = Module.ShowOnArray.OFF,
        alwaysEnabled = true
)
object ActiveModules : Module() {
    private val forgeHax = register(Settings.b("ForgeHax", false))
    val potion = register(Settings.b("PotionsMove", false))
    val hidden = register(Settings.b("ShowHidden", false))
    val mode = register(Settings.e<Mode>("Mode", Mode.RAINBOW))
    private val rainbowSpeed = register(Settings.integerBuilder("SpeedR").withValue(30).withRange(0, 255).withVisibility { mode.value == Mode.RAINBOW })
    val saturationR = register(Settings.integerBuilder("SaturationR").withValue(117).withRange(0, 255).withVisibility { mode.value == Mode.RAINBOW })
    val brightnessR = register(Settings.integerBuilder("BrightnessR").withValue(255).withRange(0, 255).withVisibility { mode.value == Mode.RAINBOW })
    val hueC = register(Settings.integerBuilder("HueC").withValue(178).withRange(0, 255).withVisibility { mode.value == Mode.CUSTOM })
    val saturationC = register(Settings.integerBuilder("SaturationC").withValue(156).withRange(0, 255).withVisibility { mode.value == Mode.CUSTOM })
    val brightnessC = register(Settings.integerBuilder("BrightnessC").withValue(255).withRange(0, 255).withVisibility { mode.value == Mode.CUSTOM })
    private val alternate = register(Settings.booleanBuilder("Alternate").withValue(true).withVisibility { mode.value == Mode.INFO_OVERLAY })

    private val chat = register(Settings.s("Chat", "162,136,227"))
    private val combat = register(Settings.s("Combat", "229,68,109"))
    private val client = register(Settings.s("Client", "56,2,59"))
    private val misc = register(Settings.s("Misc", "165,102,139"))
    private val movement = register(Settings.s("Movement", "111,60,145"))
    private val player = register(Settings.s("Player", "255,137,102"))
    private val render = register(Settings.s("Render", "105,48,109"))

    fun setColor(category: Category, color: IntArray) {
        val setting = getSettingForCategory(category) ?: return
        val r = color[0]
        val g = color[1]
        val b = color[2]
        setting.value = "$r,$g,$b"
        sendChatMessage("Set ${setting.name} colour to $r, $g, $b")
    }

    private fun getSettingForCategory(category: Category): Setting<String>? {
        return when (category) {
            Category.CHAT -> chat
            Category.COMBAT -> combat
            Category.CLIENT -> client
            Category.HIDDEN -> null // This should never be reached
            Category.MISC -> misc
            Category.MOVEMENT -> movement
            Category.PLAYER -> player
            Category.RENDER -> render
        }
    }

    fun getInfoColour(position: Int): Int {
        return if (!alternate.value) settingsToColour(false) else {
            if (isNumberEven(position)) {
                settingsToColour(true)
            } else {
                settingsToColour(false)
            }
        }
    }

    private fun settingsToColour(isOne: Boolean): Int {
        val localColor = when (infoGetSetting(isOne)) {
            TextFormatting.UNDERLINE, TextFormatting.ITALIC, TextFormatting.RESET, TextFormatting.STRIKETHROUGH, TextFormatting.OBFUSCATED, TextFormatting.BOLD -> ColorTextFormatting.colourEnumMap[TextFormatting.WHITE]?.colorLocal
                    ?: Color.WHITE
            else -> ColorTextFormatting.colourEnumMap[infoGetSetting(isOne)]?.colorLocal ?: Color.WHITE
        }
        return rgbToHex(localColor.red, localColor.green, localColor.blue)
    }

    private fun infoGetSetting(isOne: Boolean) = if (isOne) InfoOverlay.first() else InfoOverlay.second()

    fun getCategoryColour(module: Module): Int {
        return when (module.category) {
            Category.CHAT -> rgbToHex(getRgb(chat.value, 0), getRgb(chat.value, 1), getRgb(chat.value, 2))
            Category.COMBAT -> rgbToHex(getRgb(combat.value, 0), getRgb(combat.value, 1), getRgb(combat.value, 2))
            Category.CLIENT -> rgbToHex(getRgb(client.value, 0), getRgb(client.value, 1), getRgb(client.value, 2))
            Category.RENDER -> rgbToHex(getRgb(render.value, 0), getRgb(render.value, 1), getRgb(render.value, 2))
            Category.PLAYER -> rgbToHex(getRgb(player.value, 0), getRgb(player.value, 1), getRgb(player.value, 2))
            Category.MOVEMENT -> rgbToHex(getRgb(movement.value, 0), getRgb(movement.value, 1), getRgb(movement.value, 2))
            Category.MISC -> rgbToHex(getRgb(misc.value, 0), getRgb(misc.value, 1), getRgb(misc.value, 2))
            else -> rgbToHex(1, 1, 1)
        }
    }

    private fun getRgb(input: String, arrayPos: Int): Int {
        val toConvert = input.split(",").toTypedArray()
        return toConvert[arrayPos].toInt()
    }

    fun getRainbowSpeed(): Int {
        val rSpeed = reverseNumber(rainbowSpeed.value, 1, 100)
        return if (rSpeed == 0) 1 // can't divide by 0
        else rSpeed
    }

    fun getAlignedText(name: String, hudInfo: String, right: Boolean): String {
        val aligned = if (right) "$hudInfo $name" else "$name $hudInfo"
        return if (!forgeHax.value) aligned else if (right) "$aligned<" else ">$aligned"
    }

    enum class Mode {
        RAINBOW, CUSTOM, CATEGORY, INFO_OVERLAY, TRANS_RIGHTS
    }
}