package org.kamiblue.client.util.color

/**
 * @author Xiaro
 *
 * Colors based on Minecraft colors, modified for better visibility.
 *
 * Created by Xiaro on 08/08/20
 */
enum class DyeColors(val color: ColorHolder) {
    BLACK(ColorHolder(0, 0, 0)),
    RED(ColorHolder(250, 32, 32)),
    GREEN(ColorHolder(32, 250, 32)),
    BROWN(ColorHolder(180, 100, 48)),
    BLUE(ColorHolder(48, 48, 255)),
    PURPLE(ColorHolder(137, 50, 184)),
    CYAN(ColorHolder(64, 230, 250)),
    LIGHT_GRAY(ColorHolder(160, 160, 160)),
    GRAY(ColorHolder(80, 80, 80)),
    PINK(ColorHolder(255, 128, 172)),
    LIME(ColorHolder(132, 240, 32)),
    YELLOW(ColorHolder(255, 232, 0)),
    LIGHT_BLUE(ColorHolder(100, 160, 255)),
    MAGENTA(ColorHolder(220, 64, 220)),
    ORANGE(ColorHolder(255, 132, 32)),
    WHITE(ColorHolder(255, 255, 255)),
    KAMI(ColorHolder(155, 144, 255)),
    RAINBOW(ColorHolder(Int.MIN_VALUE, Int.MIN_VALUE, Int.MIN_VALUE));
}