package me.zeroeightsix.kami.util.color

import net.minecraft.util.text.TextFormatting
import java.awt.Color
import java.util.*

/**
 * @author dominikaaaa
 * Updated by dominikaaaa on 25/03/20
 * Updated by Xiaro on 18/08/20
 */
object ColorTextFormatting {
    @JvmField
    var colourEnumMap: HashMap<TextFormatting, ColourEnum?> = object : HashMap<TextFormatting, ColourEnum?>() {
        init {
            put(TextFormatting.BLACK, ColourEnum.BLACK)
            put(TextFormatting.DARK_BLUE, ColourEnum.DARK_BLUE)
            put(TextFormatting.DARK_GREEN, ColourEnum.DARK_GREEN)
            put(TextFormatting.DARK_AQUA, ColourEnum.DARK_AQUA)
            put(TextFormatting.DARK_RED, ColourEnum.DARK_RED)
            put(TextFormatting.DARK_PURPLE, ColourEnum.DARK_PURPLE)
            put(TextFormatting.GOLD, ColourEnum.GOLD)
            put(TextFormatting.GRAY, ColourEnum.GRAY)
            put(TextFormatting.DARK_GRAY, ColourEnum.DARK_GRAY)
            put(TextFormatting.BLUE, ColourEnum.BLUE)
            put(TextFormatting.GREEN, ColourEnum.GREEN)
            put(TextFormatting.AQUA, ColourEnum.AQUA)
            put(TextFormatting.RED, ColourEnum.RED)
            put(TextFormatting.LIGHT_PURPLE, ColourEnum.LIGHT_PURPLE)
            put(TextFormatting.YELLOW, ColourEnum.YELLOW)
            put(TextFormatting.WHITE, ColourEnum.WHITE)
        }
    }

    @JvmField
    var toTextMap: HashMap<ColourCode, TextFormatting?> = object : HashMap<ColourCode, TextFormatting?>() {
        init {
            put(ColourCode.BLACK, TextFormatting.BLACK)
            put(ColourCode.DARK_BLUE, TextFormatting.DARK_BLUE)
            put(ColourCode.DARK_GREEN, TextFormatting.DARK_GREEN)
            put(ColourCode.DARK_AQUA, TextFormatting.DARK_AQUA)
            put(ColourCode.DARK_RED, TextFormatting.DARK_RED)
            put(ColourCode.DARK_PURPLE, TextFormatting.DARK_PURPLE)
            put(ColourCode.GOLD, TextFormatting.GOLD)
            put(ColourCode.GRAY, TextFormatting.GRAY)
            put(ColourCode.DARK_GRAY, TextFormatting.DARK_GRAY)
            put(ColourCode.BLUE, TextFormatting.BLUE)
            put(ColourCode.GREEN, TextFormatting.GREEN)
            put(ColourCode.AQUA, TextFormatting.AQUA)
            put(ColourCode.RED, TextFormatting.RED)
            put(ColourCode.LIGHT_PURPLE, TextFormatting.LIGHT_PURPLE)
            put(ColourCode.YELLOW, TextFormatting.YELLOW)
            put(ColourCode.WHITE, TextFormatting.WHITE)
        }
    }

    enum class ColourEnum(@JvmField var colorLocal: Color) {
        BLACK(Color(0, 0, 0)),
        DARK_BLUE(Color(0, 0, 170)),
        DARK_GREEN(Color(0, 170, 0)),
        DARK_AQUA(Color(0, 170, 170)),
        DARK_RED(Color(170, 0, 0)),
        DARK_PURPLE(Color(170, 0, 170)),
        GOLD(Color(255, 170, 0)),
        GRAY(Color(170, 170, 170)),
        DARK_GRAY(Color(85, 85, 85)),
        BLUE(Color(85, 85, 255)),
        GREEN(Color(85, 255, 85)),
        AQUA(Color(85, 225, 225)),
        RED(Color(255, 85, 85)),
        LIGHT_PURPLE(Color(255, 85, 255)),
        YELLOW(Color(255, 255, 85)),
        WHITE(Color(255, 255, 255));
    }

    enum class ColourCode {
        BLACK,
        DARK_BLUE,
        DARK_GREEN,
        DARK_AQUA,
        DARK_RED,
        DARK_PURPLE,
        GOLD,
        GRAY,
        DARK_GRAY,
        BLUE,
        GREEN,
        AQUA,
        RED,
        LIGHT_PURPLE,
        YELLOW,
        WHITE
    }
}