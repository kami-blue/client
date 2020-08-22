package me.zeroeightsix.kami.util.colourUtils

/**
 * @author Xiaro
 *
 * Colors based on Minecraft colors, modified for better visibility.
 *
 * Created by Xiaro on 08/08/20
 */
enum class DyeColors(val color: ColourHolder) {
    BLACK(ColourHolder(0, 0, 0)),
    RED(ColourHolder(250, 32, 32)),
    GREEN(ColourHolder(32, 250, 32)),
    BROWN(ColourHolder(180, 100, 48)),
    BLUE(ColourHolder(48, 48, 255)),
    PURPLE(ColourHolder(137, 50, 184)),
    CYAN(ColourHolder(64, 230, 250)),
    LIGHT_GRAY(ColourHolder(160, 160, 160)),
    GRAY(ColourHolder(80, 80, 80)),
    PINK(ColourHolder(255, 128, 172)),
    LIME(ColourHolder(132, 240, 32)),
    YELLOW(ColourHolder(255, 232, 0)),
    LIGHT_BLUE(ColourHolder(100, 160, 255)),
    MAGENTA(ColourHolder(220, 64, 220)),
    ORANGE(ColourHolder(255, 132, 32)),
    WHITE(ColourHolder(255, 255, 255)),
    KAMI(ColourHolder(155, 144, 255)),
    RAINBOW(ColourHolder(Int.MIN_VALUE, Int.MIN_VALUE, Int.MIN_VALUE));
}