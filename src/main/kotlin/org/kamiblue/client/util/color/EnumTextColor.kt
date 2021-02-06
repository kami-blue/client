package org.kamiblue.client.util.color

import net.minecraft.util.text.TextFormatting

@Suppress("UNUSED")
enum class EnumTextColor(val textFormatting: TextFormatting) {
    BLACK(TextFormatting.BLACK),
    DARK_BLUE(TextFormatting.DARK_BLUE),
    DARK_GREEN(TextFormatting.DARK_GREEN),
    DARK_AQUA(TextFormatting.DARK_AQUA),
    DARK_RED(TextFormatting.DARK_RED),
    DARK_PURPLE(TextFormatting.DARK_PURPLE),
    GOLD(TextFormatting.GOLD),
    GRAY(TextFormatting.GRAY),
    DARK_GRAY(TextFormatting.DARK_GRAY),
    BLUE(TextFormatting.BLUE),
    GREEN(TextFormatting.GREEN),
    AQUA(TextFormatting.AQUA),
    RED(TextFormatting.RED),
    LIGHT_PURPLE(TextFormatting.LIGHT_PURPLE),
    YELLOW(TextFormatting.YELLOW),
    WHITE(TextFormatting.WHITE);

    override fun toString(): String = this.textFormatting.toString()
}